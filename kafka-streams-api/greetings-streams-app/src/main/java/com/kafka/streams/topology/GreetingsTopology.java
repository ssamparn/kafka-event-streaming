package com.kafka.streams.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import static com.kafka.streams.util.GreetingsStreamUtil.DESTINATION_TOPIC;
import static com.kafka.streams.util.GreetingsStreamUtil.SOURCE_TOPIC;

public class GreetingsTopology {

    public static Topology buildTopology() {

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // 1. Source Processing
        KStream<String, String> greetingsStream = streamsBuilder
                .stream(SOURCE_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        // behind the scene, source processing uses consumer apis

        greetingsStream.print(Printed.<String, String>toSysOut().withLabel("greetingsStream"));

        // 2. Stream Processing
        KStream<String, String> greetingsStreamModified = greetingsStream
                .mapValues((readOnlyKey, value) -> value.toUpperCase());

        greetingsStreamModified.print(Printed.<String, String>toSysOut().withLabel("greetingsStreamModified"));

        // 3. Sink Processing
        greetingsStreamModified.to(DESTINATION_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        // behind the scene, sink processing uses producer apis

        return streamsBuilder.build();
    }
}
