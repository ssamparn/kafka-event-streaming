package com.kafka.orders.streams;

import com.kafka.orders.streams.topology.OrdersTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.GENERAL_ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.RESTAURANT_ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.createTopics;

@Slf4j
public class OrdersKafkaStreamsApplication {

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "orders-app-2"); // consumer group
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "5000"); // commit interval
        //config.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, OrderTimeStampExtractor.class); // timestamp extractor

        createTopics(config, List.of(ORDERS_TOPIC, GENERAL_ORDERS_TOPIC, RESTAURANT_ORDERS_TOPIC));

        // create an instance of the topology
//        Topology topology = OrdersTopology.buildSplitGeneralRestaurantOrdersTopology();
//        Topology topology = OrdersTopology.buildTransformingOrderToRevenueTypeTopology();
//        Topology topology = OrdersTopology.buildAggregateOrdersByCountTopology();
        Topology topology = OrdersTopology.buildAggregateOrdersByCountingTotalRevenueMadeFromOrdersTopology();

        // Create an instance of KafkaStreams
        KafkaStreams kafkaStreams = new KafkaStreams(topology, config);

        //This closes the streams anytime the JVM shuts down normally or abruptly.
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        try {
            kafkaStreams.start();
        } catch (Exception e ) {
            log.error("Exception in starting the Streams : {}", e.getMessage(), e);
        }
    }
}
