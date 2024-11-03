package com.spring.event.streaming.consumer;

import com.spring.event.streaming.generated.PosInvoice;
import com.spring.event.streaming.serde.JsonDeserializer;
import com.spring.event.streaming.serde.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/* *
 * Problem Statement:
 * We created a POS simulator application that generates a series of invoices and sends them to a Kafka Topic.
 * Now, in this example, we want to implement a miniature form of a real-time data validation service for invoices. So, what we want to do is to read all invoices in real-time.
 * Apply some business rules to validate the invoice. If the validation is passed, send them to a Kafka topic of valid invoices. If the validation failed, send them to a Kafka topic of invalid invoices.
 * As a result, we will be able to segregate valid and invalid invoices into two different topics.
 *
 * In real life scenarios, all invalid records can be consumed by a data reconciliation application where the invoices are worked upon to rectify the issues and sent back to the pool of valid records.
 * All the correct records are consumed by other business applications and microservices to achieve some business objectives. Let’s build a real-time data validation service.
 *
 * The first thing that we need is a business rule to define an invalid invoice. What is an Invalid Invoice?
 * For our purpose, let’s define a simple rule. An invoice is considered invalid if it is marked for home delivery, but a contact number for the delivery address is missing.
 * */
@Slf4j
public class PosValidator {

    private static final String applicationID = "PosValidator";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String groupID = "PosValidatorGroup";
    private static final String[] sourceTopicNames = { "pos" };
    private static final String validTopicName = "valid-pos";
    private static final String invalidTopicName = "invalid-pos";

    public static void main(String[] args) {
        Properties consumerProperties = new Properties();

        // consumer config
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, applicationID);
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProperties.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, PosInvoice.class);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, PosInvoice> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Arrays.asList(sourceTopicNames));

        Properties producerProps = new Properties();

        // producer config
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        KafkaProducer<String, PosInvoice> producer = new KafkaProducer<>(producerProps);

        while (true) {
            ConsumerRecords<String, PosInvoice> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, PosInvoice> record : records) {
                if (record.value().getDeliveryType().equals("HOME-DELIVERY") && record.value().getDeliveryAddress().getContactNumber().equals("")) {
                    // Invalid Invoices
                    producer.send(new ProducerRecord<>(invalidTopicName, record.value().getStoreID(), record.value()));
                    log.info("Invalid Record: {}", record.value().getInvoiceNumber());
                } else {
                    // Valid Invoices
                    producer.send(new ProducerRecord<>(validTopicName, record.value().getStoreID(), record.value()));
                    log.info("Valid Record: {}", record.value().getInvoiceNumber());
                }
            }
        }
    }
}
