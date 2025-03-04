docker build -t kafka-producer .
docker build -t generate-data .

mvn clean package
docker build -t flink-processor .

docker-compose up -d