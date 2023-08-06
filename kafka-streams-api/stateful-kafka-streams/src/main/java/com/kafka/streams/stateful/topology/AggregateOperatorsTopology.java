package com.kafka.streams.stateful.topology;

import com.kafka.streams.stateful.domain.AlphabetWordAggregate;
import com.kafka.streams.stateful.serdes.SerdeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.AGGREGATE_TOPIC;

@Slf4j
public class AggregateOperatorsTopology {

    public static Topology buildTopologyForCountWithGroupByKey() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputStream = streamsBuilder.stream(AGGREGATE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        inputStream.print(Printed.<String,String>toSysOut().withLabel(AGGREGATE_TOPIC));

        KGroupedStream<String, String> groupedString = inputStream
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()));

        KTable<String, Long> stringCountKTable = groupedString.count(Named.as("count-per-alphabet"));

        stringCountKTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("words-count-per-alphabet"));

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForCountWithGroupBy() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputStream = streamsBuilder.stream(AGGREGATE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        inputStream.print(Printed.<String,String>toSysOut().withLabel(AGGREGATE_TOPIC));

        KGroupedStream<String, String> groupedString = inputStream
                .groupBy((key, value) -> value, Grouped.with(Serdes.String(), Serdes.String())); // here we are treating value as a key

        KTable<String, Long> stringCountKTable = groupedString.count(Named.as("words-count-per-alphabet"),
                Materialized.as("words-count-per-alphabet"));

        stringCountKTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("words-count-per-alphabet"));

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForReduce() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputStream = streamsBuilder.stream(AGGREGATE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        inputStream.print(Printed.<String,String>toSysOut().withLabel(AGGREGATE_TOPIC));

        KGroupedStream<String, String> groupedString = inputStream
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()));

        KTable<String, String> stringReducedKTable = groupedString.reduce((value1, value2) -> {
                    log.info("value1 : {} , value2 : {} ", value1, value2);
                    return value1.toUpperCase() +" - " + value2.toUpperCase();
                },
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("words-reduced")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.String())
        );

        stringReducedKTable.toStream().print(Printed.<String, String>toSysOut().withLabel("words-reduced"));

        return streamsBuilder.build();
    }

    public static Topology buildTopologyForAggregate() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputStream = streamsBuilder.stream(AGGREGATE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        inputStream.print(Printed.<String,String>toSysOut().withLabel(AGGREGATE_TOPIC));

        KGroupedStream<String, String> groupedString = inputStream
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()));

        Initializer<AlphabetWordAggregate> alphabetWordAggregateInitializer = AlphabetWordAggregate::new;
        Aggregator<String, String, AlphabetWordAggregate> aggregator = (key, value, alphabetWordAggregate) -> alphabetWordAggregate.updateNewEvents(key, value);

        var aggregatedStream = groupedString
                .aggregate(
                        alphabetWordAggregateInitializer,
                        aggregator,
                        Materialized.<String, AlphabetWordAggregate, KeyValueStore< Bytes, byte[]>>as("words-aggregated")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(SerdeFactory.alphabetWordAggregate())
                );

        aggregatedStream
                .toStream()
                .print(Printed.<String,AlphabetWordAggregate>toSysOut().withLabel("words-aggregated"));

        return streamsBuilder.build();
    }
}
