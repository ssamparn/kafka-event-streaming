package com.spring.event.streaming;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Arrays;
import java.util.Properties;

/* *
 *
 * Real-time aggregations: There are different techniques for computing aggregates using Kafka Streams APIs.
 * Computing aggregates in real-time over a stream of data is a complex problem to solve. Let's take a problem solution approach & streaming word count is an excellent example
 * that can help you to understand some additional basics of KTable and Aggregation.
 *
 * Problem Statement: The requirement is straightforward. We will create a Kafka topic and use the command line producer to push some text to the topic.
 * Let's assume you created a topic "word-count" and sent the following messages. e.g: "Hello Kafka Streams", "Hello World", "Hello Kafka" etc.
 *
 * On the other side, we will create a Kafka Streams application to consume data from the topic in real-time. The streams application should break the text message into words
 * and count the frequency of unique words. Finally, it should print the outcome to the console.
 *
 * For the above 3 messages, we expect the result, as shown below.
 * 
 *  |   Word   |  Count  |
 *  | ---------|---------|
 *  |   Hello  |    3    |
 *  |   Kafka  |    2    |
 *  |  Streams |    1    |
 *  |  World   |    1    |
 *  |----------|---------|
 * */
@Slf4j
public class WordCountApp {

    private static final String applicationID = "WordCount";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "word-count";
    private static final String stateStoreLocation = "tmp/state-store";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreLocation);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        // stream of sentences
        KStream<String, String> sourceKStream = streamsBuilder.stream(topicName);

        // convert stream of sentences to stream of words
        KStream<String, String> wordKStream = sourceKStream.flatMapValues(messages -> Arrays.asList(messages.toLowerCase().split(" ")));

        // grouping the stream of words based on the word
        KGroupedStream<String, String> wordGroupedStream = wordKStream.groupBy((key, word) -> word);

        // count the words
        KTable<String, Long> wordKTable = wordGroupedStream.count();

        wordKTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("word-frequency-KTable"));

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down stream");
            streams.close();
        }));
    }
}
