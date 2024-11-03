package com.spring.event.streaming.producer.simple;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/* *
 * Problem Statement:
 *    Create the simplest possible kafka producer code that sends one million string messages to a kafka topic.
 *    Kafka Producer  ------->  Kafka
 * */
@Slf4j
public class SimpleHelloProducer {

    private static final String applicationID = "SimpleHelloProducer";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "hello-kafka";
    private static final int numEvents = 1000000;

    public static void main(String[] args) {
        log.info("Creating Kafka Producer...");
        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<>(properties);

        log.info("Start sending messages...");
        for (int i = 1; i <= numEvents; i++) {
            kafkaProducer.send(new ProducerRecord<>(topicName, i, "Hello Kafka: " + i));
        }

        log.info("Finished sending messages - Closing kafka producer.");
        kafkaProducer.close();
    }

}
