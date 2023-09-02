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
    private static final String GREETING_TOPIC = "greeting";

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(producerProps);

        Greeting greeting = buildGreeting("Hello, Schema Registry!");
        byte[] message = greeting.toByteBuffer().array();

        ProducerRecord<String, byte[]> producerRecord =
                new ProducerRecord<>(GREETING_TOPIC, message);

        RecordMetadata recordMetadata = producer.send(producerRecord).get();
        log.info("recordMetaData : " + recordMetadata.toString());
    }

    private static Greeting buildGreeting(String message) {
        return Greeting.newBuilder()
                .setId(UUID.randomUUID())
                .setGreeting(message)
                //.setCreatedDateTimeLocal(LocalDateTime.now()) // LocalDateTime
                .setCreatedDateTime(Instant.now()) // UTC dateTime
                .setCreatedDate(LocalDate.now()) // LocalDate
                .setCost(BigDecimal.valueOf(3.999)) // 123.45 has a precision of 5 and a scale of 2.
                .build();
    }
}
