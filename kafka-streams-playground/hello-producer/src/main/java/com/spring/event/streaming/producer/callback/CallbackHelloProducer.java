package com.spring.event.streaming.producer.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.sql.Timestamp;
import java.util.Properties;

/* *
 * Producer Callback:
 *   In this approach, you can supply a callback function to your producer to be invoked when the acknowledgment arrives.
 * In this case, the send method returns immediately. But the internal I/O thread will call the lambda function supplied as part of callback when the acknowledgment arrives,
 * and you can take necessary action in your lambda method.
 * */
@Slf4j
public class CallbackHelloProducer {

    private static final String applicationID = "CallbackHelloProducer";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "callback-hello-kafka";
    private static final int numEvents = 100;

    public static void main(String[] args) throws InterruptedException {
        log.trace("Creating Kafka Producer...");

        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // We want to raise an exception - So, do not retry.
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);

        // We want to raise an exception - So, take acknowledgement only when message is persisted to all brokers in ISR
        properties.put(ProducerConfig.ACKS_CONFIG, "all");

        /* *
        Follow below steps to generate exception.
          1. Start three node cluster
          2. Create topic with --config min.insync.replicas=3
          3. Start producer application
          4. Shutdown one broker node while producer is running - It will cause NotEnoughReplicasException
        * */

        KafkaProducer<Integer, String> producer = new KafkaProducer<>(properties);

        for (int i = 1; i <= numEvents; i++) {
            Thread.sleep(500);
            String message = "Hello kafka Sync: " + i;
            producer.send(new ProducerRecord<>(topicName, i, message), (recordMetadata, e) -> {
                // executes every time a record is successfully sent or an exception is thrown
                if (e != null) {
                    log.error("Error while sending message string {}", message);
                } else {
                    // record was successfully sent
                    log.info("{} persisted with topic: {} partition: {} offset: {} and timestamp on {}",
                            message,
                            recordMetadata.topic(),
                            recordMetadata.partition(),
                            recordMetadata.offset(),
                            new Timestamp(recordMetadata.timestamp())
                    );
                }
            });
        }
        log.info("Finished Application - Closing Producer.");
        producer.close();
    }
}
