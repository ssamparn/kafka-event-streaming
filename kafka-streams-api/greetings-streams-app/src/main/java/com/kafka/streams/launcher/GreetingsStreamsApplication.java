package com.kafka.streams.launcher;

import com.kafka.streams.topology.GreetingsTopology;
import com.kafka.streams.util.GreetingsStreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.streams.util.GreetingsStreamUtil.APP_NAME;
import static com.kafka.streams.util.GreetingsStreamUtil.DESTINATION_TOPIC;
import static com.kafka.streams.util.GreetingsStreamUtil.SOURCE_TOPIC;

@Slf4j
public class GreetingsStreamsApplication {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME); // consumer group
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
//        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, EXACTLY_ONCE_V2);

        // create topics
        GreetingsStreamUtil.createTopics(config, List.of(SOURCE_TOPIC, DESTINATION_TOPIC));

        // create topology
        Topology greetingsTopology = GreetingsTopology.buildTopology();


        KafkaStreams kafkaStreams = new KafkaStreams(greetingsTopology, config);

        // this closes the streams anytime the JVM shuts down normally or abruptly.
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        try {
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception in starting the Streams : {}", e.getMessage(), e);
        }

    }


}
