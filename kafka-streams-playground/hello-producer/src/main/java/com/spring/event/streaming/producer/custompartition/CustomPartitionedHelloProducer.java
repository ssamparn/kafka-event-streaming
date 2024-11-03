package com.spring.event.streaming.producer.custompartition;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/* *
 * Handling Custom Partitioning:
 * Kafka topic partitions are a tool to support scalability by evenly distributing data across partitions and assigning partitions for the consumers for parallel reading and processing.
 * Partitions are used for parallel writing and reading. You can also leverage this partitioning in a variety of ways.
 * For example, if you have some experience in database tables, you already understand the importance of partitioning and bucketing your table into smaller chunks and group similar records on some criteria.
 * The default Kafka partitioner allows you to group your data in partitions using the message key. However, in some scenarios, you may want to change the default behavior and implement your own partitioning algorithm.
 * Kafka producer API permits you to implement your custom partitioning strategy. You can apply custom bucketing or a partitioning approach by implementing your own partitioner class.The point is straightforward.
 * Even if your data is not appropriately partitioned while it is entering your kafka cluster from producers, you can change it and repartition your data according to your requirement while you are implementing your stream processing use cases.
 * */
@Slf4j
public class CustomPartitionedHelloProducer {

    private static final String applicationID = "PartitionProducer";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "partitioned-producer";
    private static final int numEvents = 100;

    public static void main(String[] args) {
        log.trace("Creating Kafka Producer...");

        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, OddEvenPartitioner.class);

        RecordMetadata metadata;
        try (KafkaProducer<Integer, String> producer = new KafkaProducer<>(properties)) {
            for (int i = 1; i <= numEvents; i++) {
                Thread.sleep(1000);
                metadata = producer.send(new ProducerRecord<>(topicName, i, "Hello Kafka:" + i)).get();
                log.info("Message: {}  persisted with offset: {} in partition: {}", i, metadata.offset(), metadata.partition());
            }
        } catch (Exception e) {
            log.info("Can't send message - Received exception {} \n", e.getMessage());
        }
    }
}
