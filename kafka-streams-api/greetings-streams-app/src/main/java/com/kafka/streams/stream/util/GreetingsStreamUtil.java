package com.kafka.streams.stream.util;

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
    public static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(producerProperties());

    /* *
     * programmatically creating kafka topics
     * admin client class is part of Kafka Clients library
     * */
    public static void createTopics(Properties config, List<String> greetingsTopics) {
        AdminClient admin = AdminClient.create(config);
        int partitions = 2;
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
            log.error("Exception creating topics : {}", e.getMessage(), e);
        }
    }

    public static Map<String, Object> producerProperties(){
        Map<String,Object> propertiesMap = new HashMap<>();
        propertiesMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propertiesMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propertiesMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return propertiesMap;
    }

    public static RecordMetadata publishMessageSync(String topicName, String key, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, message);
        RecordMetadata recordMetadata = null;

        try {
            log.info("Producer Record : {}", producerRecord);
            recordMetadata = kafkaProducer.send(producerRecord).get();
        } catch (InterruptedException e) {
            log.error("Interrupted Exception in publishMessageSync : {}", e.getMessage(), e);
        } catch (ExecutionException e) {
            log.error("Execution Exception in publishMessageSync : {}", e.getMessage(), e);
        }catch(Exception e){
            log.error("Exception in publishMessageSync : {}", e.getMessage(), e);
        }
        return recordMetadata;
    }
}
