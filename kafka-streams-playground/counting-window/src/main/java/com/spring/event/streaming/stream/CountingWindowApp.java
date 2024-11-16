package com.spring.event.streaming.stream;

import com.spring.event.streaming.generated.SimpleInvoice;
import com.spring.event.streaming.serde.AppSerdes;
import com.spring.event.streaming.util.InvoiceTimeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Properties;

/* *
 * We know about computing aggregates on a KTable and KStream. The aggregation methods are good to compute aggregates on an infinite stream of data.
 * For example, you are calculating total sales by the store. You started today, and the total sale was 100K. The next day, the same total will increase, let's say it goes to 180K.
 * This 180K is the sum of the total sale since we started computing it. The next day, it is going to increase further, let's say 270K.
 * Does it make any sense to keep growing the same total all the time? No.
 * Such things are more meaningful when we compute them in the time-based window. For example, daily sales, hourly sales, per minute, or even per second, right? The point is straightforward.
 * You often need to create smaller chunks or buckets of data and calculate aggregates on the bucket only. Such buckets are more often termed as windows, and the time is the most commonly used attribute for windowing.
 * However, there are 2 additional questions about time.
 *   1. Which time (timestamp) are we talking about?
 *   2. How do I know the time?
 * Let's explain the first part - Which time are we talking about? The invoice is generated at the store. Let's say, and it is created at 9.59.00 AM. Just one minute before 10.00 AM. Right?
 * You are sending it to Kafka. But your network hanged or went down for 10 minutes. But ultimately, your invoice reaches to Kafka cluster at 10.10. Now you have a stream processing application that is computing the totals.
 * However, your application is running a little slow. So It reads the invoice at 10.13 AM and calculates the sum. Now, look at the situation. For the same invoice, we have a notion of three timestamps, which one do you want to consider.
 * We are talking about minutes here, and you might be thinking that these differences in three timestamps are unrealistic, or at least it is not going to happen on a day to day basis. Right?
 * But think about seconds and milliseconds. These three timestamps are going to exists and will be different. That difference might be as small as few milliseconds, but they will be different.
 * The point is, which timestamp do you want to consider? We have a proper name for these three timestamps.
 *
 *    Event Time: 1. The event time is the exact time when the event was generated at the source system. For example, an invoice is made at the POS terminal. In this case, the event time can be stamped by the POS system,
 * and the time becomes an integral part of the invoice. In such cases, we represent the event time using a field in the event object itself. For example, our POS simulator generates invoices with a filed CreationTime.
 * Having CreationTime in your event is the best thing that you can have. Why? Because it represents the actual time when the event occurred. Right? However, in many cases, your event generating system may not already have a timestamp field.
 * Also, some times, it may not be feasible to modify such applications to add a timestamp field. What can you do in those cases? In some of those cases, you may be using producer API to transmit your events to Kafka.
 * The producer API is smart enough to put a timestamp in the record metadata. So, your Kafka records are timestamped by the producer automatically. Thus, the producer timestamp is the best possible alternative that you can have when the message does not come with its own timestamp.
 * These two alternatives are used to work with the notion of Event time. But the real world is so complicated. You may have a situation when you don't have a timestamp in the event, and you are not using Kafka Producer API.
 * You might be using a third-party tool to bring data to Kafka, and the tool does not create a timestamp in the record metadata.
 * Possible? Absolutely yes. So what option do you have now?
 *
 *   Ingestion Time: 2. The next possible option is the ingestion time. Ingestion time is the time when the event reaches to your Kafka Broker. Obviously, you can't explicitly set the ingestion time.
 * It must be assigned by the Kafka broker, and the broker does that for you. However, you need to set a configuration for this.
 * message.timestamp.type = LogAppendTime
 * You can set this configuration at the topic level. But remember this. When the topic is configured to use LogAppendTime, the timestamp in your record metadata is overwritten by the broker using the broker system time.
 * That means you can either create a timestamp metadata using the producer time or the broker time. But you can't have both because there is only one timestamp in the message metadata.
 * Great! In most cases, you should be using the event time. However, when your use case demands to use the ingestion time, or you do not have a mechanism to implement the event time, you should be using ingestion time.
 *
 *   Processing Time: 3. Finally, the third and last option is to use the processing time. The Processing time is the time when you receive the message in your Kafka streams topology.
 * Your messages are sitting in the broker, you will be reading it using your Streams API, and when it comes to the topology source processor, it is timestamped with the current time of your computer,
 * and that's what we call the processing time. Right? Great!
 *
 * We answered the first question - Which time are we talking about? Now the next question, How do I know the time?
 * These timestamps are sitting inside the message or in the message metadata. Right? For example, the event time is hidden inside the creationDate field in your message body.
 * Similarly, the producer time or the log-append time is hidden inside the record metadata. How do you tell your application about which timestamp do you want to use? Right? That's the question.
 * Well, that's absolutely straightforward. All you do is to set the following configuration.
 *  streamProperties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
 * What are we doing in this configuration? We are simply telling How to extract the timestamp. The WallclockTimestampExtractor is available to you as an out of the box timestamp extractor, which simply gives current system time.
 * So, if you are configuring WallclockTimestampExtractor, that means you are going to use the processing time. If you want to use the producer time or the log-append time, you must provide a different extractor which reads timestamp from the record metadata. Right?
 *
 * You have 3 out of the box extractor for this purpose.
 *  | Sl No   |    Timestamp Extractor    |      Description                                |
 *  ----------------------------------------------------------------------------------------
 *  |  1      | FailOnInvalidTimestamp            |    Raise exception when timestamp is invalid   |
 *  |  2      | LogAndSkipOnInvalidTimestamp      |    Skip the message if timestamp is invalid    |
 *  |  3      | UsePreviousTimeOnInvalidTimestamp |    Use the timestamp of previous message when the timestamp in current message is invalid  |
 * All of these are to extract time from the metadata. That means you will be using producer time or the log-append time.
 * Finally, how to use message timestamp? How can we extract creationTime from the message and use it for time-based windows. Any guesses?
 *
 * Yes. You will be creating a custom timestamp extractor and configure it here. That's all.
 *
 * Summary:
 * Kafka allows you to work on 3 types of time semantics. Event Time, Ingestion Time and Processing Time.
 * To use the event time, you must have a timestamp in your message itself and use a custom timestamp extractor. Alternatively, you can use producer timestamps to simulate an event time and use any of the above 3 extractors.
 *
 * If you want to use Ingestion time semantics, then you should configure the topic for LogAppendTime and use one of these timestamp extractors.
 *
 * Finally, if you are interested in using processing time, then all you do is to configure the WallclockTimestampExtractor.
 *
 * Kafka Stream API offers two types of windows.
 *   1. Time window
 *   2. Session window
 *
 * Problem Statement:
 * Compute the number of invoices generated in a 5-minute window by each store. You can simulate the scenario in a controlled environment.
 * Create a Kafka topic named as simple-invoice. Send the following Invoices to the Kafka topic. See sample-invoices.txt
 * Each invoice comes as key/value pair. The value before the colon is a store-id which is the key. Rest of the value is a JSON Serialize invoice event.
 * The event time of the invoice is recorded in the creationTime field. We want to develop a stream processing application that uses event-time semantics to compute the number of invoices in a 5-minute window for each store.
 * For the given invoices, you can expect the following outcome.
 * ---------------------------------------------------------------------------------
 * StoreId   |   WindowId   |     Window Start       |     Window End      | Count |
 * ---------------------------------------------------------------------------------
 * STR1534    | 2050479689  |    2019-02-05T10:00Z   |  2019-02-05T10:05Z  |  3   |
 * STR1534    | 2050479849  |    2019-02-05T10:05Z   |  2019-02-05T10:10Z  |  1   |
 * STR1535    | 2050479689  |    2019-02-05T10:10Z   |  2019-02-05T10:15Z  |  3   |
 *
 * Let's talk about the solution. We already learned that grouping your data is a mandatory step for computing aggregates. The problem statement clearly states that we want to group our data by StoreID.
 * However, we also want to create a subgroup of a five-minute window. In other words, for each group or store-id, we want to create a series of a five-minute time window.
 * Once you conceptualize the notion of the window as a subgroup, rest is straightforward. As the record flows, we want them to map to a key and time window combination.
 * Once the record is assigned to the window, computing count() or any other aggregate is straightforward.
 * */
@Slf4j
public class CountingWindowApp {

    private static final String applicationID = "CountingWindowApp";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "simple-invoice";
    private static final String stateStoreName = "tmp/state-store";
    
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreName);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        /* *
         * invoice source kstream
         * in this example, we want to work with the event time. The event time is embedded in the invoice schema, and hence, we must create and specify a custom timestamp extractor.
         * */
        KStream<String, SimpleInvoice> simpleInvoiceKStream = streamsBuilder
                .stream(topicName, Consumed.with(AppSerdes.String(), AppSerdes.SimpleInvoice()).withTimestampExtractor(new InvoiceTimeExtractor()));

        // grouping invoice source kstream with storeId and again subgrouping based on time
        KTable<Windowed<String>, Long> windowedLongKTable = simpleInvoiceKStream.groupByKey(Grouped.with(AppSerdes.String(), AppSerdes.SimpleInvoice()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count();

        windowedLongKTable.toStream().foreach((windowKey, value) ->
                log.info("Store Id: {} " +
                        "Window Id: {} " +
                        "Window start: {} " +
                        "Window end: {} " +
                        "Count: {}",
                        windowKey.key(),
                        windowKey.window().hashCode(),
                        Instant.ofEpochMilli(windowKey.window().start()).atOffset(ZoneOffset.UTC),
                        Instant.ofEpochMilli(windowKey.window().end()).atOffset(ZoneOffset.UTC),
                        value)
        );

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Streams");
            streams.close();
        }));

        /* *
         * V Imp Note: In this example, we created a fixed size, non-overlapping, gap-less time window. Such windows are known as tumbling time window.
         * Just to summarize, tumbling windows are a fixed-size, non-overlapping, gap-less windows. You can define a tumbling window using the size of the window as we did in TimeWindows.of() method.
         * You might be wondering what will be the start time of the window? I mean, we just give a window size and let Kafka decide the start time of the window. Right?
         * Kafka API would look at the timestamp of the first message and use a rounding algorithm to arrive at a starting time for the window. They call it “normalizing the window start to the epoch.”
         * So, “Normalized to the epoch” means that the first window starts at a rounded epoch, which is lower than the timestamp of the first message. Once the first window start is normalized, all other windows are at a fixed interval of defined size.
         * Great! That’s all about the tumbling window.
         * */
    }
}
