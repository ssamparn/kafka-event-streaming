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
 * Combine 2 independent Kafka streams or KStream into a single KStream.
 * Let's say we have 2 KStreams which are being read from 2 different topics, and we have a business use case to channel that data that's coming out from these 2 topics into one single topic.
 * In these kind of scenarios, we can use the merge operator to channel data that's coming from 2 different topics into one single topic.
 * */
@Slf4j
public class GreetingsStreamsMergeApp {

    public static void main(String[] args) {
        Properties streamProperties = new Properties();
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME); // consumer group
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // create topics programmatically
        GreetingsStreamUtil.createTopics(streamProperties, List.of(SOURCE_TOPIC, SOURCE_TOPIC_SPANISH, DESTINATION_TOPIC));

        // create topology
        Topology simpleGreetingsTopology = GreetingsTopology.buildGreetingsMergeTopology();

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