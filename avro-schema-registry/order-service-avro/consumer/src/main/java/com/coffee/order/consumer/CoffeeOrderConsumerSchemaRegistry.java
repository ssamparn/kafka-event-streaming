package com.coffee.order.consumer;

import com.coffee.order.domain.generated.CoffeeOrder;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CoffeeOrderConsumerSchemaRegistry {

    private static final Logger log = LoggerFactory.getLogger(CoffeeOrderConsumerSchemaRegistry.class);
    private static final String COFFEE_ORDERS_TOPIC = "coffee-orders-sr";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "coffee.consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        KafkaConsumer<String, CoffeeOrder> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(COFFEE_ORDERS_TOPIC));

        log.info("Consumer Started");

        while (true) {
            ConsumerRecords<String, CoffeeOrder> records = consumer.poll(Duration.ofMillis(100));
            try {
                for (ConsumerRecord<String, CoffeeOrder> record : records) {
                    CoffeeOrder coffeeOrder = record.value();
                    log.info("Consumed message: \n" + record.key() + " : " + coffeeOrder.toString());
                }
            } catch (Exception e){
                log.error("Exception is : {} ", e.getMessage(), e);
            } finally {
                consumer.commitSync();
                log.info("Committed the offset catch");
            }
        }
    }

}
