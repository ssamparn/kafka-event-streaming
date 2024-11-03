package com.spring.event.streaming.producer.sync;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.sql.Timestamp;
import java.util.Properties;

/* *
 * Synchronous Send of Kafka API:
 * Default Kafka producer send() API is asynchronous and non-blocking. When you call the send API, it merely adds the ProducerRecord into the buffer and returns immediately.
 * Asynchronous and non-blocking send is efficient. However, it also leaves the possibility to lose some messages without even knowing that some of your messages never reached the Kafka broker.
 * Kafka producer internally takes care of this problem using automatic retries. However, if retries are also failing, you can still lose some messages.
 * In real life, this loss is not more than 1-2 percent of the total messages, and in some use cases, missing 1-2 % of data may not be a problem.
 * However, in many use cases, you don't want to miss even a single record. So, the question is, How to deal with this problem?
 * The first thing is to take a decision on what do you want to do with the failing records? In most of the cases, you may want to store the failing records in separate storage and take some necessary action at a later stage. Right?
 * Now the next question is, How do we implement it, because the send() method is asynchronous and returns immediately without waiting for the acknowledgment.
 *
 * There are 2 ways to deal with this problem.
 *
 * In the first approach, you can call a get() method over the send() method. The get() method is synchronous and blocking call, which waits for the acknowledgment.
 * The get() method throws an exception in case of failure, or returns the metadata in case of success.
 * This approach makes your send() method a synchronous call and allows us to take whatever action you want to take in case of failure.
 * However, such blocking send method call is suitable for scenarios where your messages are produced at a slower pace.
 * Asynchronous sends were more efficient as the producer can leverage grouping multiple messages and optimize network round trips to achieve better throughput.
 * So, to handle high throughput use cases, and also knowing which messages failed to deliver, Kafka API offers a second alternative, Producer Callback. See CallbackHelloProducer Implementation.
 * */
@Slf4j
public class SyncHelloProducer {

    private static final String applicationID = "SyncHelloProducer";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "sync-hello-kafka";
    private static final int numEvents = 100;

    public static void main(String[] args) {
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
         * Follow below steps to generate exception.
         * 1. Start three node cluster
         * 2. Create topic with --config min.insync.replicas=3
         * 3. Start producer application
         * 4. Shutdown one broker node while producer is running - It will cause NotEnoughReplicasException
         * */

        RecordMetadata metadata;
        try (KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(properties)) {
            for (int i = 1; i <= numEvents; i++) {
                Thread.sleep(500);
                metadata = kafkaProducer.send(new ProducerRecord<>(topicName, i, "Hello kafka Sync: " + i)).get();
                log.info("Message {} persisted with offset {} and timestamp on {}", i, metadata.offset(), new Timestamp(metadata.timestamp()));
            }
        } catch (Exception e) {
            log.info("Can't send message - Received exception \n {}", e.getMessage());
        }
    }
}
