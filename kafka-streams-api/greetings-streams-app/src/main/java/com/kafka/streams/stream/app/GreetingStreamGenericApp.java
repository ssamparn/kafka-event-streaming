package com.kafka.streams.stream.app;

import com.kafka.streams.exceptionhandler.StreamsProcessorCustomErrorHandler;
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

import static com.kafka.streams.stream.util.GreetingsStreamUtil.APP_NAME;
import static com.kafka.streams.stream.util.GreetingsStreamUtil.DESTINATION_TOPIC;
import static com.kafka.streams.stream.util.GreetingsStreamUtil.SOURCE_TOPIC;
import static com.kafka.streams.stream.util.GreetingsStreamUtil.SOURCE_TOPIC_SPANISH;
import static com.kafka.streams.stream.util.GreetingsStreamUtil.bootstrapServers;

@Slf4j
public class GreetingStreamGenericApp {

    public static void main(String[] args) {
        Properties streamProperties = new Properties();
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME); // consumer group
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // read only the new messages
        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        streamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, String.valueOf(Runtime.getRuntime().availableProcessors()));
//        streamProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "2");

        // error-handling config with default error handler while deserializing messages
        // we are not shutting down the application in case of deserialization error while reading the message
        // config.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);

        // error-handling config with custom error handler while deserializing messages
//        streamProperties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, StreamsDeserializationErrorHandler.class);

        // error-handling config with custom error handler while serializing messages
//        streamProperties.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, StreamsSerializationExceptionHandler.class);

        // create topics
        GreetingsStreamUtil.createTopics(streamProperties, List.of(SOURCE_TOPIC, SOURCE_TOPIC_SPANISH, DESTINATION_TOPIC));

        // create topology
        // Topology greetingsTopology = GreetingsTopology.buildStringSerdeTopology();
        // Topology greetingsTopology = GreetingsTopology.buildCustomSerdeTopology();
        // Topology greetingsTopology = GreetingsTopology.buildGenericSerdeTopology();
        Topology greetingsTopology = GreetingsTopology.buildStreamProcessingErrorTopology();

        KafkaStreams kafkaStreams = new KafkaStreams(greetingsTopology, streamProperties);
        kafkaStreams.setUncaughtExceptionHandler(new StreamsProcessorCustomErrorHandler());

        // this closes the streams anytime the JVM shuts down normally or abruptly.
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        try {
            kafkaStreams.start();
        } catch (Exception e) {
            log.error("Exception in starting the Streams : {}", e.getMessage(), e);
        }
    }
}
