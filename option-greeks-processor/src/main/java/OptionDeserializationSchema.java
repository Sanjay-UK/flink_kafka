import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserializes option data from Kafka messages.
 * This class can be customized to match your exact Kafka message format.
 */
public class OptionDeserializationSchema extends AbstractDeserializationSchema<Option> {
  private static final long serialVersionUUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(OptionDeserializationSchema.class);

  private transient ObjectMapper objectMapper;

  @Override
  public void open(InitializationContext context) {
    objectMapper = JsonMapper.builder().build().registerModule(new JavaTimeModule());
  }

  @Override
  public Option deserialize(byte[] message) throws IOException {
    try {
      // First try direct deserialization
      JsonNode node = objectMapper.readTree(message);
      
      // IMPORTANT: Customize this mapping to match your exact Kafka message format
      // The field names here should match the JSON fields in your Kafka messages
      
      Option option = new Option();
      
      // Map fields from your JSON to the Option class
      if (node.has("symbol")) {
        option.symbol = node.get("symbol").asText();
      } else if (node.has("optionSymbol")) {
        option.symbol = node.get("optionSymbol").asText();
      }
      
      if (node.has("underlyingSymbol")) {
        option.underlyingSymbol = node.get("underlyingSymbol").asText();
      } else if (node.has("underlying")) {
        option.underlyingSymbol = node.get("underlying").asText();
      }
      
      if (node.has("type")) {
        option.type = node.get("type").asText();
      } else if (node.has("optionType")) {
        option.type = node.get("optionType").asText();
      } else if (node.has("callPut")) {
        option.type = node.get("callPut").asText();
      }
      
      if (node.has("strike")) {
        option.strike = node.get("strike").asDouble();
      } else if (node.has("strikePrice")) {
        option.strike = node.get("strikePrice").asDouble();
      }
      
      if (node.has("underlyingPrice")) {
        option.underlyingPrice = node.get("underlyingPrice").asDouble();
      } else if (node.has("stockPrice")) {
        option.underlyingPrice = node.get("stockPrice").asDouble();
      }
      
      if (node.has("optionPrice")) {
        option.optionPrice = node.get("optionPrice").asDouble();
      } else if (node.has("price")) {
        option.optionPrice = node.get("price").asDouble();
      } else if (node.has("lastPrice")) {
        option.optionPrice = node.get("lastPrice").asDouble();
      }
      
      if (node.has("impliedVolatility")) {
        option.impliedVolatility = node.get("impliedVolatility").asDouble();
      } else if (node.has("iv")) {
        option.impliedVolatility = node.get("iv").asDouble();
      }
      
      if (node.has("timeToExpiry")) {
        option.timeToExpiry = node.get("timeToExpiry").asDouble();
      } else if (node.has("dte")) {
        // Convert days to years if needed
        option.timeToExpiry = node.get("dte").asDouble() / 365.0;
      } else if (node.has("daysToExpiry")) {
        option.timeToExpiry = node.get("daysToExpiry").asDouble() / 365.0;
      }
      
      if (node.has("riskFreeRate")) {
        option.riskFreeRate = node.get("riskFreeRate").asDouble();
      } else {
        // Default risk-free rate if not provided
        option.riskFreeRate = 0.04; // 4%
      }
      
      // Validate required fields
      if (option.symbol == null || option.type == null || option.strike == null || 
          option.underlyingPrice == null || option.optionPrice == null || 
          option.impliedVolatility == null || option.timeToExpiry == null) {
        
        LOG.warn("Missing required fields in message: {}", new String(message));
        
        // For debugging - log the attempted parsing
        LOG.debug("Parsed values: symbol={}, type={}, strike={}, underlyingPrice={}, optionPrice={}, iv={}, timeToExpiry={}", 
                 option.symbol, option.type, option.strike, option.underlyingPrice, 
                 option.optionPrice, option.impliedVolatility, option.timeToExpiry);
                 
        return null; // Skip this message
      }
      
      // Make sure option type is standardized to "CALL" or "PUT"
      if (option.type != null) {
        option.type = standardizeOptionType(option.type);
      }
      
      return option;
    } catch (Exception e) {
      LOG.error("Error deserializing message: {}", new String(message), e);
      return null; // Skip this message
    }
  }
  
  /**
   * Standardize option type to "CALL" or "PUT"
   */
  private String standardizeOptionType(String type) {
    if (type == null) return null;
    
    String upperType = type.toUpperCase();
    if (upperType.contains("CALL") || upperType.equals("C")) {
      return "CALL";
    } else if (upperType.contains("PUT") || upperType.equals("P")) {
      return "PUT";
    }
    return upperType;
  }
}