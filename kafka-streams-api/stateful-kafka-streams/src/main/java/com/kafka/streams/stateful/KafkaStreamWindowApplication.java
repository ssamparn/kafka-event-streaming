package com.kafka.streams.stateful;

import com.kafka.streams.stateful.topology.WindowTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.WINDOW_WORDS_TOPIC;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.createTopics;

@Slf4j
public class KafkaStreamWindowApplication {

    public static void main(String[] args) {
        Topology tumblingWindowTopology = WindowTopology.tumblingWindow();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "windows"); // consumer group
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "0");

        createTopics(config, List.of(WINDOW_WORDS_TOPIC));
        var kafkaStreams = new KafkaStreams(tumblingWindowTopology, config);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        log.info("Starting Windowed streams");
        kafkaStreams.start();
    }
}
