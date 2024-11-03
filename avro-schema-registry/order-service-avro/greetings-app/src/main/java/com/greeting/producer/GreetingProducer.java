package com.greeting.producer;

import com.greeting.model.Greeting;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GreetingProducer {

    private static final Logger log = LoggerFactory.getLogger(GreetingProducer.class);
    private static final String GREETING_TOPIC = "greetings";

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:8082");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(producerProperties);

        Greeting greeting = buildGreeting("Hello, Schema Registry!");
        byte[] message = greeting.toByteBuffer().array();

        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(GREETING_TOPIC, message);

        RecordMetadata recordMetadata = producer.send(producerRecord).get();
        log.info("Record Meta Data : " + recordMetadata.toString());
    }

    private static Greeting buildGreeting(String message) {
        return Greeting.newBuilder()
                .setId(UUID.randomUUID())
                .setGreeting(message)
                .setCreatedDateTime(Instant.now()) // UTC dateTime
                .setCreatedDate(LocalDate.now()) // LocalDate
                .setCost(BigDecimal.valueOf(3.999)) // 123.45 has a precision of 5 and a scale of 2.
                .build();
    }
}
