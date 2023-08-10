package com.kafka.ktable.streams;

import com.kafka.ktable.streams.topology.KTableTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.ktable.streams.utils.KTableStreamUtil.WORDS_OUTPUT_TOPIC;
import static com.kafka.ktable.streams.utils.KTableStreamUtil.WORDS_TOPIC;
import static com.kafka.ktable.streams.utils.KTableStreamUtil.createTopics;

@Slf4j
public class KTableStreamsApplication {

    public static void main(String[] args) {
        Topology kTableTopology = KTableTopology.buildKTableTopology();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "words-ktable-app"); // consumer group
        config.put(StreamsConfig.CLIENT_ID_CONFIG, "words-ktable-client");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        createTopics(config, List.of(WORDS_TOPIC, WORDS_OUTPUT_TOPIC));

        KafkaStreams kafkaStreams = new KafkaStreams(kTableTopology, config);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        log.info("!!! Starting KTable Streams !!!");

        kafkaStreams.start();
    }
}
