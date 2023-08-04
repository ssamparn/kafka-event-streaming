package com.kafka.streams.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class GreetingsStreamUtil {

    public static final String APP_NAME = "greetings-app";
    public static final String SOURCE_TOPIC = "greetings";
    public static String GREETINGS_SPANISH = "greetings-spanish";
    public static final String DESTINATION_TOPIC = "greetings-uppercase";


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
}
