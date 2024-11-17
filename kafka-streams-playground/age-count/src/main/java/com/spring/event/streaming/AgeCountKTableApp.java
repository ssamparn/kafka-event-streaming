package com.spring.event.streaming;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

/* *
 * We now know how to perform count(), reduce() and aggregate() on a KStream.
 * Now it's time to see similar things on KTable. So, let’s start with the count() on a KTable.
 * Problem Statement:
 * Create a topic named "person-age" and send the following messages.
 *   Sashank:26
 *   Prashant:41
 *   John:38
 *   Jackson:26
 *   Paul:41
 *   Sashank:27
 *   Prashant:42
 * Each record shown here is a key/value pair. The key is the name of a person, and the value is the age of the person. We want to calculate the count() by age.
 * So create a kafka streams application to count the number of persons by age.
 * The outcome should look like this.
 *   | Age | Count|
 *   |------------|
 *   | 26  |  2   |
 *   | 38  |  1   |
 *   | 41  |  2   |
 * Counting the number of persons by age is a super simple example. However, this example helps us to realize the difference between KTable aggregate and KStream aggregate.
 * Well, the requirement that we have in our hand is an update stream problem. Why? Because the age of a person is not constant. It will change. The person with age 41 will become 42.
 * And in that case, the age for the person should be updated to 42. This update will also change the aggregates, right? The count for the 41 age should be reduced by one, and at the same time, the count for the age 42 should increase by one, right?
 * So, we need to model the solution using KTable aggregation.
 * */
@Slf4j
public class AgeCountKTableApp {

    private static final String applicationID = "AgeCountDemo";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "person-age";
    private static final String stateStoreLocation = "tmp/state-store";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreLocation);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<String, Long> kTable = streamsBuilder.table(topicName, Consumed.with(Serdes.String(), Serdes.String()))
                .groupBy((person, age) -> new KeyValue<>(age, ""), Grouped.with(Serdes.String(), Serdes.String()))
                .count();

        /* *
         * The value of the grouped KTable has no meaning that's why provided as "" empty string. Why? As all we want to do is to count the records.
         * We can also put "1" instead of empty string "". It does not matter.
         * */

        kTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("Age Count"));

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Stream");
            streams.close();
        }));

        /* *
         * Count on a KTable is straightforward. However, what happens if Sashank turns 27. You may want to send another record to change his age to 27.
         * How would it impact the count? This scenario is like the one where employees swapped their department (see kstream-aggregate module), and we have seen that it does not give current results for a KStream aggregate.
         * However, in this example, we opened a KTable instead of a KStream, and the person’s name is a primary key. Hence, the record for the Sashank will be updated, and the count is also updated correctly.
         * Earlier, count for the age of 26 was 2. Since Sashank’s age is changed from 26 to 27, the count for age 26 will be reduced by one.
         * How exactly that happens? How is that count reduced? We will learn more about it in the ktable-aggregate module, while we implement the aggregate() method.
         * The count() method is a kind of black box, and we don’t see much inside the count() method.
         * However, the aggregate() method gives us a little more visibility on the decreasing values.
         * */
    }
}
