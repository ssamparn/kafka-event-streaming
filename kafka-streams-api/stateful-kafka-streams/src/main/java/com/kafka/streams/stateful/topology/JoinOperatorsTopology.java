package com.kafka.streams.stateful.topology;

import com.kafka.streams.stateful.domain.Alphabet;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.ValueJoiner;

import java.time.Duration;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.ALPHABETS_ABBREVATIONS_TOPIC;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.ALPHABETS_TOPIC;

@Slf4j
public class JoinOperatorsTopology {

    // Example of Stateful operations that can be performed in Kafka Streams:
    // Joining Event Streams. For example, combining data from two independent streams based on a key.
    // Joins in Kafka Streams are used to combine data from multiple topics.
    // Joins are performed if there is a matching key in both the topics.
    // Joining of Data: join(), leftJoin() and outerJoin()

    // Difference between kafka stream merge() and join():
    // A join produces results by combining Events with the same key to produce a new Event, possibly of a different type.
    // A merge of Event Streams combines the Events from multiple Event Streams into a single Event Stream, but the individual Events are unchanged and remain independent of each other.

    // Types of Joins

    /**
     * |  Join Types           |  Operations that can be performed |
     * ------------------------|------------------------------------
     * | KStream-KTable        | join, left-join                   |
     * | KStream-GlobalKTable  | join, left-join                   |
     * | KTable-KTable         | join, left-join, outer-join       |
     * | KStream-KStream       | join, left-join, outer-join       |
     * -------------------------------------------------------------
     */

    // e.g: 2 topics
    // alphabets: [A, A is the first letter in the English Alphabet]
    // alphabet_abbreviations: [A, Apple]
    // Joined Stream:
    // {
    //   "abbreviation": "Apple",
    //   "description": "A is the first letter in the English Alphabet"
    // }
    // Join will get triggered if there is a matching record for the same key.
    // To achieve a resulting data model like above, we would need a ValueJoiner.
    // This is also called Inner-Join.

    public static Topology buildTopologyForJoiningKStreamWithKTable() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> alphabetsAbbreviationStream = streamsBuilder.stream(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsAbbreviationStream.print(Printed.<String, String>toSysOut().withLabel(ALPHABETS_ABBREVATIONS_TOPIC));

        KTable<String, String> alphabetsKTable = streamsBuilder
                .table(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("alphabets-store"));
        alphabetsKTable.toStream().print(Printed.<String, String>toSysOut().withLabel(ALPHABETS_TOPIC));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;

        KStream<String, Alphabet> joinedStream = alphabetsAbbreviationStream.join(alphabetsKTable, valueJoiner);
        // The idea of Inner Join or Join is anytime we get a new event in the KStream, it will look for a matching key in the KTable and will join the records using ValueJoiner.

        joinedStream.print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations"));

//      [alphabets-with-abbreviations]: C, Alphabet[abbreviation=Cat, description=C is the third letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Bus, description=B is the second letter in English Alphabets.]
//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Apple, description=A is the first letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Baby, description=B is the second letter in English Alphabets.]
//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Airplane, description=A is the first letter in English Alphabets.]
        return streamsBuilder.build();

//        VImp Note: In the case of Joining KStream with KTable, new events into the KTable will not trigger any join.
//        But new events into KStream will trigger a join based on the matching key found in the KTable.
    }

    public static Topology buildTopologyForJoiningKStreamWithGlobalKTable() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> alphabetsAbbreviationStream = streamsBuilder.stream(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsAbbreviationStream.print(Printed.<String, String>toSysOut().withLabel(ALPHABETS_ABBREVATIONS_TOPIC));

        GlobalKTable<String, String> alphabetsGlobalKTable = streamsBuilder
                .globalTable(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("alphabets-global-store"));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;
        KeyValueMapper<String, String, String> keyValueMapper = (leftKey, rightKey) -> leftKey; // using the key from ALPHABETS_ABBREVATIONS_TOPIC.

        KStream<String, Alphabet> joinedStream = alphabetsAbbreviationStream.join(alphabetsGlobalKTable, keyValueMapper, valueJoiner);
        // The idea of Inner Join or Join is anytime we get a new event in the KStream, it will look for a matching key in the GLobalKTable and will join the records using ValueJoiner.

        joinedStream.print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations"));

//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Apple, description=A is the first letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Bus, description=B is the second letter in English Alphabets.]
//      [alphabets-with-abbreviations]: C, Alphabet[abbreviation=Cat, description=C is the third letter in English Alphabets.]
//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Airplane, description=A is the first letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Baby, description=B is the second letter in English Alphabets.]

        return streamsBuilder.build();

//        VImp Note: In the case of Joining KStream with GlobalKTable, new events into the KTable will not trigger any join.
//        But new events into KStream will trigger a join based on the matching key found in the KTable.
    }

    public static Topology buildTopologyForJoiningKTableWithAnotherKTable() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<String, String> alphabetsAbbreviationKTable = streamsBuilder
                .table(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("alphabets-abbreviations-store"));
        alphabetsAbbreviationKTable.toStream().print(Printed.<String, String>toSysOut().withLabel("alphabets-abbreviations"));

        KTable<String, String> alphabetsKTable = streamsBuilder
                .table(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("alphabets-store"));
        alphabetsKTable.toStream().print(Printed.<String, String>toSysOut().withLabel("alphabets"));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;

        KTable<String, Alphabet> joinedKTable = alphabetsAbbreviationKTable.join(alphabetsKTable, valueJoiner);

//        var joinedTable = alphabetsAbbreviation
//                .leftJoin(alphabetsTable,
//                        valueJoiner);

        joinedKTable.toStream().print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations"));

//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Apple, description=A is the first letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Bus, description=B is the second letter in English Alphabets.]
//      [alphabets-with-abbreviations]: C, Alphabet[abbreviation=Cat, description=C is the third letter in English Alphabets.]
//      [alphabets-with-abbreviations]: A, Alphabet[abbreviation=Apple, description=A is the first letter in English Alphabets.]
//      [alphabets-with-abbreviations]: B, Alphabet[abbreviation=Bus, description=B is the second letter in English Alphabets.]
//      [alphabets-with-abbreviations]: C, Alphabet[abbreviation=Cat, description=C is the third letter in English Alphabets.]

        // Here we are seeing duplicate records after joining as join is triggered from both ends of the table.

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForJoiningKStreamWithAnotherKStream() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Note: A KStream to KStream join is a little different from other types of join.
        // As we know a KStream is an infinite stream which represents a log of everything that happened.

        // For join to happen between two KStream instances, 2 things are to be certain.
        // 1. It is expected that both the KStreams have the same key, and
        // 2. Events should happen within a certain time window. (By default, any records gets published to a kafka topic has a timestamp attached to it.)


        KStream<String, String> alphabetsAbbreviationKStream = streamsBuilder.stream(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsAbbreviationKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets-abbreviations"));

        KStream<String, String> alphabetsKStream = streamsBuilder.stream(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets"));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;
        JoinWindows fiveSecondWindow = JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)); // 5th second is not inclusive
        StreamJoined<String, String, String> joinedParams = StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()).withName("alphabets-stream-join").withStoreName("alphabets-stream-join");

        KStream<String, Alphabet> joinedKStream = alphabetsAbbreviationKStream.join(alphabetsKStream, valueJoiner, fiveSecondWindow, joinedParams);
        joinedKStream.print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations-kstream"));

//        [alphabets-with-abbreviations-kstream]: A, Alphabet[abbreviation=Apple, description=A is the first letter in English Alphabets.]
//        [alphabets-with-abbreviations-kstream]: B, Alphabet[abbreviation=Bus, description=B is the second letter in English Alphabets.]
//        [alphabets-with-abbreviations-kstream]: C, Alphabet[abbreviation=Cat, description=C is the third letter in English Alphabets.]

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForLeftJoin() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Note: Join is triggered when there is a record on the left side of the join is received, even if there is no matching record received on the right side of the join.
        // This will still produce a combined record. The combined record will have a null value for the right side of the value.

        KStream<String, String> alphabetsAbbreviationKStream = streamsBuilder.stream(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsAbbreviationKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets-abbreviations"));

        KStream<String, String> alphabetsKStream = streamsBuilder.stream(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets"));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;
        JoinWindows fiveSecondWindow = JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)); // 5th second is not inclusive
        StreamJoined<String, String, String> joinedParams = StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()).withName("alphabets-stream-join").withStoreName("alphabets-stream-join");

        KStream<String, Alphabet> joinedKStream = alphabetsAbbreviationKStream.leftJoin(alphabetsKStream, valueJoiner, fiveSecondWindow, joinedParams);
        joinedKStream.print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations-kstream"));

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForOuterJoin() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Note: Outer Join is triggered when there is a record on either side of the join is received.
        // When a record is received on either side of the join, and if there is no matching record on the other side of the join,
        // then the combined record will have a null value for either side of the missing value.

        KStream<String, String> alphabetsAbbreviationKStream = streamsBuilder.stream(ALPHABETS_ABBREVATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsAbbreviationKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets-abbreviations"));

        KStream<String, String> alphabetsKStream = streamsBuilder.stream(ALPHABETS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        alphabetsKStream.print(Printed.<String, String>toSysOut().withLabel("alphabets"));

        ValueJoiner<String, String, Alphabet> valueJoiner = Alphabet::new;
        JoinWindows fiveSecondWindow = JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)); // 5th second is not inclusive
        StreamJoined<String, String, String> joinedParams = StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()).withName("alphabets-stream-join").withStoreName("alphabets-stream-join");

        KStream<String, Alphabet> joinedKStream = alphabetsAbbreviationKStream.outerJoin(alphabetsKStream, valueJoiner, fiveSecondWindow, joinedParams);
        joinedKStream.print(Printed.<String, Alphabet>toSysOut().withLabel("alphabets-with-abbreviations-kstream"));

        return streamsBuilder.build();
    }
}
