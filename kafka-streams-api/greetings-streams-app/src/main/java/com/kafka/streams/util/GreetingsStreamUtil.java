package com.kafka.streams.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class GreetingsStreamUtil {

    public static final String APP_NAME = "greetings-app";
    public static final String SOURCE_TOPIC = "greetings";
    public static String SOURCE_TOPIC_SPANISH = "greetings-spanish";
    public static final String DESTINATION_TOPIC = "greetings-uppercase";
    private static KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps());

    public static void createTopics(Properties config, List<String> greetingsTopics) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 2;
        short replication = 1;

        List<NewTopic> newTopics = greetingsTopics
                .stream()
                .map(topic -> new NewTopic(topic, partitions, replication))
                .collect(Collectors.toList());

        CreateTopicsResult createTopicsResult = admin.createTopics(newTopics);
        try {
            createTopicsResult.all().get();
            log.info("topics are created successfully");
        } catch (Exception e) {
            log.error("Exception creating topics : {} ",e.getMessage(), e);
        }
    }

    public static Map<String, Object> producerProps(){

        Map<String,Object> propsMap = new HashMap<>();
        propsMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        propsMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return propsMap;
    }

    public static RecordMetadata publishMessageSync(String topicName, String key, String message ){

        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topicName, key, message);
        RecordMetadata recordMetadata = null;

        try {
            log.info("producerRecord : {}", producerRecord);
            recordMetadata = producer.send(producerRecord).get();
        } catch (InterruptedException e) {
            log.error("InterruptedException in publishMessageSync : {}", e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error("ExecutionException in publishMessageSync : {}", e.getMessage(), e);
        }catch(Exception e){
            log.error("Exception in publishMessageSync : {}", e.getMessage(), e);
        }
        return recordMetadata;
    }
}
