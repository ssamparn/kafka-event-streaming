package com.kafka.greetings.stream.springboot.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.greetings.stream.springboot.domain.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import static com.kafka.greetings.stream.springboot.config.GreetingsStreamsConfig.GREETINGS;
import static com.kafka.greetings.stream.springboot.config.GreetingsStreamsConfig.GREETINGS_OUTPUT;

@Component
@Slf4j
public class GreetingsStreamsTopology {

    private final ObjectMapper objectMapper;

    @Autowired
    public GreetingsStreamsTopology(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        KStream<String, Greeting> greetingsStream = streamsBuilder.stream(GREETINGS, Consumed.with(Serdes.String(), new JsonSerde<>(Greeting.class, objectMapper)));
        greetingsStream.print(Printed.<String, Greeting>toSysOut().withLabel("greeting-streams"));

        KStream<String, Greeting> modifiedStream = greetingsStream.mapValues((readOnlyKey, value) -> {
            if (value.getMessage().equals("Error")) {
                try {
                    throw new IllegalStateException("Error Occurred");
                } catch (Exception e) {
                    log.error("Exception is : {} ", e.getMessage(), e);
                    throw e;
                }
            }
            return new Greeting(value.getMessage().toUpperCase(), value.getTimeStamp());
        });
        modifiedStream.print(Printed.<String, Greeting>toSysOut().withLabel("modified-greeting-streams"));

        modifiedStream.to(GREETINGS_OUTPUT, Produced.with(Serdes.String(), new JsonSerde<>(Greeting.class, objectMapper)));
    }
}
