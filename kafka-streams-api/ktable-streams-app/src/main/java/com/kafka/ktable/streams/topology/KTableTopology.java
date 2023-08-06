package com.kafka.ktable.streams.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;

import static com.kafka.ktable.streams.utils.KTableStreamUtil.WORDS_TOPIC;

public class KTableTopology {
    public static Topology buildKTableTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<String, String> wordsTable = streamsBuilder
                .table(WORDS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("words-store"));

        wordsTable
                .filter((key, value) -> value.length() > 2)
                .mapValues((readOnlyKey, value) -> value.toUpperCase())
                .toStream()
//                .to(WORDS_OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
                .print(Printed.<String, String>toSysOut().withLabel("words-ktable"));

        return streamsBuilder.build();
    }

    public static Topology buildGlobalKTableTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        GlobalKTable<String, String> wordsGlobalKTable = streamsBuilder
                .globalTable(WORDS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()), Materialized.as("words-store"));

        wordsGlobalKTable
                .queryableStoreName();

        return streamsBuilder.build();
    }
}
