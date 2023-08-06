package com.kafka.ktable.streams.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class KTableStreamUtil {

    public static String WORDS_TOPIC = "words-topic";

    public static String WORDS_OUTPUT_TOPIC = "words-output-topic";

    public static void createTopics(Properties config, List<String> ktableTopics) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 1;
        short replication = 1;

        List<NewTopic> newTopics = ktableTopics
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
