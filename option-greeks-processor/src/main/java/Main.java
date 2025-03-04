import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    // Read configuration from environment variables with defaults
    static final String BROKERS = System.getenv("KAFKA_SERVER") != null ? 
                                 System.getenv("KAFKA_SERVER") : "kafka:9092";
    static final String KAFKA_TOPIC = System.getenv("KAFKA_TOPIC") != null ? 
                                     System.getenv("KAFKA_TOPIC") : "options";
    static final String KAFKA_GROUP_ID = System.getenv("KAFKA_GROUP_ID") != null ? 
                                        System.getenv("KAFKA_GROUP_ID") : "options-greeks-processor";
    
    static final String POSTGRES_HOST = System.getenv("POSTGRES_HOST") != null ? 
                                       System.getenv("POSTGRES_HOST") : "postgres";
    static final String POSTGRES_USER = System.getenv("POSTGRES_USER") != null ? 
                                       System.getenv("POSTGRES_USER") : "postgres";
    static final String POSTGRES_PASSWORD = System.getenv("POSTGRES_PASSWORD") != null ? 
                                          System.getenv("POSTGRES_PASSWORD") : "postgres";
    static final String POSTGRES_DB = System.getenv("POSTGRES_DB") != null ? 
                                     System.getenv("POSTGRES_DB") : "postgres";
    
    static final String POSTGRES_URL = "jdbc:postgresql://" + POSTGRES_HOST + ":5432/" + POSTGRES_DB;

    public static void main(String[] args) throws Exception {
        // Allow time for services to be ready
        Thread.sleep(30000);
        
        LOG.info("Starting Option Greeks processor...");
        LOG.info("Connecting to Kafka at: {}", BROKERS);
        LOG.info("Reading from Kafka topic: {}", KAFKA_TOPIC);
        LOG.info("Connecting to PostgreSQL at: {}", POSTGRES_URL);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create Kafka source
        KafkaSource<Option> source = KafkaSource.<Option>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics(KAFKA_TOPIC)
                .setGroupId(KAFKA_GROUP_ID)
                .setStartingOffsets(OffsetsInitializer.latest()) // Use latest to avoid processing historical data
                .setValueOnlyDeserializer(new OptionDeserializationSchema())
                .build();

        // Read from Kafka
        DataStreamSource<Option> optionsStream = env.fromSource(
                source, 
                WatermarkStrategy.noWatermarks(), 
                "options-kafka-source"
        );

        // Calculate Greeks
        DataStream<OptionGreeks> greeksStream = optionsStream
                .map(new GreeksCalculator())
                .name("option-greeks-calculator");

        // Print sample results to console (for debugging)
        greeksStream.print();

        // Store each option with its greeks in PostgreSQL
        greeksStream.addSink(
            JdbcSink.sink(
                "INSERT INTO option_greeks (symbol, underlying_symbol, option_type, strike, " +
                "underlying_price, option_price, implied_volatility, time_to_expiry, risk_free_rate, " +
                "delta, gamma, theta, vega, rho, calculated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                (statement, optionGreeks) -> {
                    statement.setString(1, optionGreeks.symbol);
                    statement.setString(2, optionGreeks.underlyingSymbol);
                    statement.setString(3, optionGreeks.type);
                    statement.setDouble(4, optionGreeks.strike);
                    statement.setDouble(5, optionGreeks.underlyingPrice);
                    statement.setDouble(6, optionGreeks.optionPrice);
                    statement.setDouble(7, optionGreeks.impliedVolatility);
                    statement.setDouble(8, optionGreeks.timeToExpiry);
                    statement.setDouble(9, optionGreeks.riskFreeRate);
                    statement.setDouble(10, optionGreeks.delta);
                    statement.setDouble(11, optionGreeks.gamma);
                    statement.setDouble(12, optionGreeks.theta);
                    statement.setDouble(13, optionGreeks.vega);
                    statement.setDouble(14, optionGreeks.rho);
                },
                JdbcExecutionOptions.builder()
                    .withBatchSize(1000)
                    .withBatchIntervalMs(200)
                    .withMaxRetries(5)
                    .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(POSTGRES_URL)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(POSTGRES_USER)
                    .withPassword(POSTGRES_PASSWORD)
                    .build()
            )
        ).name("postgres-options-greeks-sink");

        // Also store historical data for time-series analysis
        greeksStream.addSink(
            JdbcSink.sink(
                "INSERT INTO option_greeks_history (symbol, underlying_price, option_price, " +
                "delta, gamma, theta, vega, rho, time_to_expiry, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                (statement, optionGreeks) -> {
                    statement.setString(1, optionGreeks.symbol);
                    statement.setDouble(2, optionGreeks.underlyingPrice);
                    statement.setDouble(3, optionGreeks.optionPrice);
                    statement.setDouble(4, optionGreeks.delta);
                    statement.setDouble(5, optionGreeks.gamma);
                    statement.setDouble(6, optionGreeks.theta);
                    statement.setDouble(7, optionGreeks.vega);
                    statement.setDouble(8, optionGreeks.rho);
                    statement.setDouble(9, optionGreeks.timeToExpiry);
                },
                JdbcExecutionOptions.builder()
                    .withBatchSize(1000)
                    .withBatchIntervalMs(200)
                    .withMaxRetries(5)
                    .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(POSTGRES_URL)
                    .withDriverName("org.postgresql.Driver")
                    .withUsername(POSTGRES_USER)
                    .withPassword(POSTGRES_PASSWORD)
                    .build()
            )
        ).name("postgres-options-history-sink");

        // Execute Flink job
        LOG.info("Starting Flink job: Options-Greeks-Processor");
        env.execute("Options-Greeks-Processor");
    }
}