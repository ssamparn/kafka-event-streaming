package com.kafka.streams.stateful.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.WINDOW_WORDS_TOPIC;

@Slf4j
public class WindowTopology {

    // Example of Stateful operations that can be performed in Kafka Streams:
    // Windowing is the concept of Grouping events in a certain time window that share the same key.
    // Example: a. Calculating the number of orders made in every minute/hour/day.
    //          b. Calculating the total revenue generated every minute/hour/day.
    // Windowing of Data: windowedBy()
    // In order to group events by time, we need to extract the time from the Kafka record.

    // Time Concepts:
    // 1. Event Time: Represents the time of the record gets produced. (Set by the Kafka Producer).
    // 2. Ingestion Time: Represents the time the record gets appended to the Kafka topic in the log.
    // 3. Processing Time: Represents the time the record gets read and processed by the Kafka Consumer.

    /* *
     * TimeStamp Extractor in Kafka Streams:
     * a. FailOnInvalidTImestamp.
     * b. LogAndSkipOnInvalidTimestamp
     * c. WallclockTimestampExtractor
     * */

    /* *
    * Window Types:
    * 1. Tumbling Window :
    *       a. This window type is a fixed sized window.
    *       b. Windows never overlap.
    *       c. This window type will group records by matching key.
    *       d. Time Windows/Buckets are created from the clock of the application's running machine.
    * Use Case 1: Total number of tickets sold on the opening day.
    * Use Case 2: Total revenue made on the opening day.
    * Use Case 3: Total sales made on a Black Friday.
    * 2. Hopping Window :
    *       a.
    * 3. Sliding Window.
    * 4. Session Window.
    * 5. Sliding Join Window.
    * 6. Sliding Aggregation Window.
    */

    public static Topology tumblingWindow() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Duration windowSize = Duration.ofSeconds(5);
        Duration graceWindowsSize = Duration.ofSeconds(2);
        TimeWindows tumblingWindow = TimeWindows.ofSizeWithNoGrace(windowSize);
//        TimeWindows hoppingWindow = TimeWindows.ofSizeAndGrace(windowSize, graceWindowsSize);

        KStream<String, String> wordsStream = streamsBuilder.stream(WINDOW_WORDS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        wordsStream.print(Printed.<String,String>toSysOut().withLabel("words@source"));

        wordsStream
                .groupByKey()
                .windowedBy(tumblingWindow)
                .count()
//                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()))
                .toStream()
                .peek(((key, value) -> {
                    log.info("tumbling window : key : {}, value : {}", key, value);
                    printLocalDateTimes(key, value);
                }))
                .print(Printed.<Windowed<String>, Long>toSysOut().withLabel("tumblingWindow"));

        return streamsBuilder.build();
    }

    private static void printLocalDateTimes(Windowed<String> key, Long value) {
        Instant startTime = key.window().startTime();
        Instant endTime = key.window().endTime();

        LocalDateTime startLdt = LocalDateTime.ofInstant(startTime, ZoneId.of(ZoneId.SHORT_IDS.get("ECT")));
        LocalDateTime endLdt = LocalDateTime.ofInstant(endTime, ZoneId.of(ZoneId.SHORT_IDS.get("ECT")));
        log.info("Start Local Date Time : {} , End Local Date Time : {}, Count : {}", startLdt, endLdt, value);
    }
}
