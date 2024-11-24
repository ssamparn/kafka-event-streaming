package com.kafka.streams.stream.app;

import com.kafka.streams.stream.topology.GreetingsTopology;
import com.kafka.streams.stream.util.GreetingsStreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.streams.stream.util.GreetingsStreamUtil.*;

/* *
 * Problem Statement:
 * Build a simple Kafka Streams app named Greetings App.
 * Read from a Kafka topic named "greetings" and then perform a stream processing logic and write it to another kafka topic named "greetings-uppercase.
 * Just perform the uppercase operation and then write it to the destination kafka topic.
 * */
@Slf4j
public class GreetingsStreamsOperationsApp {

    public static void main(String[] args) {
        Properties streamProperties = new Properties();
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME); // consumer group
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // create topics programmatically
        GreetingsStreamUtil.createTopics(streamProperties, List.of(SOURCE_TOPIC, DESTINATION_TOPIC));

        // create topology
        Topology simpleGreetingsTopology = GreetingsTopology.buildFilterMapFlatMapTopology();

        KafkaStreams kafkaStreams = new KafkaStreams(simpleGreetingsTopology, streamProperties);

        try {
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception in starting the Streams : {}", e.getMessage(), e);
        }

        // closes the streams anytime the JVM shuts down normally or abruptly.
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }
}