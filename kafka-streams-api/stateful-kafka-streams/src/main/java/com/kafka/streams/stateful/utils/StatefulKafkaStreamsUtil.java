package com.kafka.streams.stateful.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class StatefulKafkaStreamsUtil {

    public static String AGGREGATE_TOPIC = "aggregate";

    public static String ALPHABETS_TOPIC = "alphabets"; // A => First letter in the english alphabet
    public static String ALPHABETS_ABBREVATIONS_TOPIC = "alphabets-abbreviations"; // A => Apple

    public static void createTopics(Properties config, List<String> topics) {
        AdminClient admin = AdminClient.create(config);
        var partitions = 1;
        short replication  = 1;

        var newTopics = topics
                .stream()
                .map(topic -> new NewTopic(topic, partitions, replication))
                .collect(Collectors.toList());

        var createTopicResult = admin.createTopics(newTopics);
        try {
            createTopicResult.all().get();
            log.info("topics are created successfully");
        } catch (Exception e) {
            log.error("Exception creating topics : {} ",e.getMessage(), e);
        }
    }
}
