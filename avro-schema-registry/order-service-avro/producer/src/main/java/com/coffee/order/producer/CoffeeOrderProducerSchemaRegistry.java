package com.coffee.order.producer;

import com.coffee.order.domain.generated.CoffeeOrder;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.coffee.order.util.CoffeeOrderUtil.buildNewCoffeeOrder;

public class CoffeeOrderProducerSchemaRegistry {

    private static final String COFFEE_ORDERS_TOPIC = "coffee-orders-sr";
    private static final Logger log = LoggerFactory.getLogger(CoffeeOrderProducerSchemaRegistry.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:8082");
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerProperties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        KafkaProducer<String, CoffeeOrder> producer = new KafkaProducer<>(producerProperties);

        CoffeeOrder coffeeOrder = buildNewCoffeeOrder();
        System.out.println("Coffee Order Sent " + coffeeOrder);

        ProducerRecord<String, CoffeeOrder> producerRecord = new ProducerRecord<>(COFFEE_ORDERS_TOPIC, coffeeOrder);
        var recordMetaData = producer.send(producerRecord).get();
        log.info("recordMetaData : {}" , recordMetaData);
    }
}
