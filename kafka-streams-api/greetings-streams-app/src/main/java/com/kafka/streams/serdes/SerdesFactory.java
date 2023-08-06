package com.kafka.streams.serdes;

import com.kafka.streams.domain.Greeting;
import com.kafka.streams.serdes.json.JsonDeserializer;
import com.kafka.streams.serdes.json.JsonSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SerdesFactory {

    public static Serde<Greeting> greetingSerde() {
        return new GreetingSerde();
    }

    public static Serde<Greeting> greetingSerdeUsingGenerics() {
        JsonSerializer<Greeting> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Greeting> jsonDeSerializer = new JsonDeserializer<>(Greeting.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
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
