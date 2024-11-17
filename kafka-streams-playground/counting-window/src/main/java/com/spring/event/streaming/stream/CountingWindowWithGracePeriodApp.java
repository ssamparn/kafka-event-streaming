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
 * We created a tumbling window example (see current class with uncommenting TimeWindows.of(Duration.ofMinutes(5). Using already deprecated of() on purpose) to count the invoices in a 5-minute window. Let's start the Kafka services, create the topic, start the application (in current class with of()).
 * Now, I am going to start a console producer and send some invoices. Here is the first invoice with the event time as 10:01 (see part-1 of sample-invoices-with-grace-period.txt).
 * The next invoice is sent at 10:08. We are counting invoices in a 5-minute window. So, we expect 2 windows here.
 * The first window starts at 10:00 and ends five minutes later at 10:05. We got 1 invoice in this window, and that's what the count is showing. The next window starts at 10:05 and ends at 10:10.
 * That's how tumbling window works. Right? Fixed-size, non-overlapping, gap-less windows for a given duration. We got 1 invoice, and hence, the count is 1.
 *
 * Now, we have a question here. What if we receive a new invoice that was generated at 10:03 (see part-2 of sample-invoices-with-grace-period.txt). We don't expect that record in normal circumstances because we already received a record for 10:08.
 * How can we get a new record for an earlier time? Well, it may happen if the record is arriving late to the Kafka cluster.
 * The latecomers are part of real-life scenarios, and we must be able to deal with them. The question is this. How Kafka Streams API deals with the latecomers?
 *
 * Let's send the late incoming invoice, and we will observe what happens. So the same window id is updated with the new count, and we term this approach as the notion of continuous update.
 * What it means is to compute the aggregates for the window and forward the results to downstream for reporting. If a late record arrives, update the results and deliver it again to the downstream to refresh the reports. Right?
 * And this approach of continuous refresh is perfectly fine in most of the cases.
 *
 * However, we have a question here. Do you want to allow latecomers for infinite time? What do you think? I guess the answer will be a No in most of the cases.
 * In most cases, the late coming records beyond a particular time have no value. For example, in healthcare. You are estimating patients' condition on her vital signals, and a message arriving late by more than five minutes may not have any significance because the formula doesn't depend upon older values. Right?
 * So the point is straightforward. How can we ignore latecomers if they are arriving too late? The answer is the grace period operator.
 * Let's implement a grace period of 2 minutes in this example & understand how it works?
 *
 * So, I send one invoice, and Kafka created a window starting from 10:00 AM to 10:05. However, the current stream time is stuck at 10:01, which is the maximum record timestamp received so far.
 * Let's send the next record. This new record will advance the stream time to 10:08. Right? Kafka will also create a new window because the time stream time clock pushed beyond the first window boundary.
 * Let's send a latecomer record. What do you expect? In standard cases, Kafka should update the count for the first window. However, we created the window with 2 minutes of the grace period.
 * So, Kafka will update the count for the first window only if a late coming record for the first window arrives before 10:07. In our case, the clock is already passed 10:08, and now we got a late coming record.
 * So, this guy is too late and arrived beyond the grace period. Hence, Kafka will ignore the late coming invoice. In other words, we can say that the first window of 10:00 to 10:05 is now fully closed.
 * Why? Because the stream time clock is passed the window end time as well as the grace period of two minutes. Now the aggregate count of the first window is fixed and final. It is never going to update.
 * Hence, we consider it a fully closed window. Great! Hope you get the notion of stream time and the grace period.
 *
 * Let's summarize.
 * V Imp Note - Notion of Stream Time & Wall Clock Time: Kafka Streams API implements the notion of stream time rather than the wall clock time, and the stream time advances with the record timestamp.
 * For example, if you received three records at 10:01, 10:08, and 10:03, then the current stream time is 10:08, which is the maximum of the timestamps seen in the stream.
 * If you don't get more records, the stream time is stuck at 10:08. Make sense. The stream time clock is different from your wall clock time. The stream time advances when you receive a new record with a greater timestamp.
 * Kafka windows are kept open to accept latecomers and implement the notion of continuous updates. However, you can set the grace period and close older windows as the stream time clock advances beyond the grace period.
 * The default grace period is 24 hours. That's all about the stream time and the grace period.
 *
 * So the record will be skipped or discarded because of expired window.
 * When working with windows, you can specify a grace period for the window. This grace period controls how long Kafka Streams will wait for out-of-order data records for a given window.
 * If a record arrives after the grace period of a window has passed, the record is discarded and will not be processed in that window.
 * Specifically, a record is discarded if its timestamp dictates it belongs to a window, but the current stream time is greater than the end of the window plus the grace period.
 *
 * */
@Slf4j
public class CountingWindowWithGracePeriodApp {

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

        // grouping invoice source kstream with storeId and again subgrouping based on time and also providing a grace period to handle late arriving messages to the kafka cluster.
        KTable<Windowed<String>, Long> windowedLongKTable = simpleInvoiceKStream.groupByKey(Grouped.with(AppSerdes.String(), AppSerdes.SimpleInvoice()))
//                .windowedBy(TimeWindows.of(Duration.ofMinutes(5))) // using of() which is deprecated on purpose to demo the notion of continuous update for late incoming events
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(5), Duration.ofMinutes(2)))
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
    }
}
