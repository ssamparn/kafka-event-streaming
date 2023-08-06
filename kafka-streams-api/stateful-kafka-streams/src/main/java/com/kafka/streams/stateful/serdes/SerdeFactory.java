package com.kafka.streams.stateful.serdes;

import com.kafka.streams.stateful.domain.Alphabet;
import com.kafka.streams.stateful.domain.AlphabetWordAggregate;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SerdeFactory {
    public static Serde<AlphabetWordAggregate> alphabetWordAggregate() {
        JsonSerializer<AlphabetWordAggregate> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<AlphabetWordAggregate> jsonDeSerializer = new JsonDeserializer<>(AlphabetWordAggregate.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }

    public static Serde<Alphabet> alphabet() {
        JsonSerializer<Alphabet> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Alphabet> jsonDeSerializer = new JsonDeserializer<>(Alphabet.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }
}
