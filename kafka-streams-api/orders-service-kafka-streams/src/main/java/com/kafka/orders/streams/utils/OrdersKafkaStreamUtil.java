package com.kafka.orders.streams.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class OrdersKafkaStreamUtil {

    public static final String ORDERS_TOPIC = "orders";
    public static final String GENERAL_ORDERS_TOPIC = "general-orders";

    public static final String RESTAURANT_ORDERS_TOPIC = "restaurant-orders";

    public static final String GENERAL_ORDERS_COUNT_TOPIC = "general-orders-count";

    public static final String RESTAURANT_ORDERS_COUNT_TOPIC = "restaurant-orders-count";
    public static final String GENERAL_ORDERS_REVENUE_TOPIC = "general-orders-revenue";

    public static final String RESTAURANT_ORDERS_REVENUE_TOPIC = "restaurant-orders-revenue";

    public static final String STORES = "stores";

    public static void createTopics(Properties config, List<String> topics) {

        AdminClient admin = AdminClient.create(config);
        var partitions = 2;
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
