package com.kafka.streams.serdes;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafka.streams.domain.Greeting;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SerdesFactory {

//    public static Serde<Greeting> greetingSerdeUsingGenerics() {
//
//        JsonSerializer<Greeting> jsonSerializer = new JsonSerializer<>();
//
//        JsonDeserializer<Greeting> jsonDeSerializer = new JsonDeserializer<>(Greeting.class);
//        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
//    }

    public static Serde<Greeting> greetingSerde() {

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return new GreetingSerde();
    }

//    public static Serde<AlphabetWordAggregate> alphabetWordAggregate() {
//
//        JsonSerializer<AlphabetWordAggregate> jsonSerializer = new JsonSerializer<>();
//
//        JsonDeserializer<AlphabetWordAggregate> jsonDeSerializer = new JsonDeserializer<>(AlphabetWordAggregate.class);
//        return  Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
//    }
//
//
//    public static Serde<Alphabet> alphabet() {
//
//        JsonSerializer<Alphabet> jsonSerializer = new JsonSerializer<>();
//
//        JsonDeserializer<Alphabet> jsonDeSerializer = new JsonDeserializer<>(Alphabet.class);
//        return  Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
//    }
}
