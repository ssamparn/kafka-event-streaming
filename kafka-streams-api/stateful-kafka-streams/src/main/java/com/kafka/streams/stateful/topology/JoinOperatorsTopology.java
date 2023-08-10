package com.kafka.streams.stateful.topology;

import com.kafka.streams.stateful.domain.Alphabet;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.ValueJoiner;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.AGGREGATE_TOPIC;
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

        return streamsBuilder.build();
    }
}
