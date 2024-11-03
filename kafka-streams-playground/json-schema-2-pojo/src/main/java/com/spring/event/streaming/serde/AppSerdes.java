package com.spring.event.streaming.serde;

import com.spring.event.streaming.generated.DeliveryAddress;
import com.spring.event.streaming.generated.PosInvoice;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.HashMap;
import java.util.Map;

/* *
 * We can generate POJOs or Java types for our data from the provided schema. This is the first half of the problem.
 * The second part is to provide Serdes for the Java types.
 *
 * A Serdes is a combination of a serializer and a deserializer class. Apache Kafka Streams API includes several built-in Serdes implementations for Java primitives and basic types
 * such as StringSerde, IntegerSerde, DoubleSerde, and BytesSerde. These Serdes and their corresponding serializer/deserializer classes are defined in org.apache.kafka.common.serialization package.
 * However, these Serdes are not enough for a real-life requirement. A typical Kafka Streams application would end up using custom Java objects, and hence you would need a custom Serdes for your Java objects.
 *
 * Implementing a custom Serdes is a 3-step process.
 *   1. Write a serializer by implementing the org.apache.kafka.common.serialization.Serializer<T> interface.
 *   2. Write a deserializer by implementing the org.apache.kafka.common.serialization.Deserializer<T> interface.
 * Once you have serializer and deserializer,
 *   3. Write a Serde by implementing the org.apache.kafka.common.serialization.Serde<T> interface.
 *
 * Step 1 and 2 are not needed. Why? Because a serializer and a deserializer is a standard thing.

 * We have JSON serializers / deserializers & AVRO serializers / deserializers available with confluent community license. We just have to reuse them.
 * So, the focus is on the 3rd step.
 *
 * You have 3 ways to achieve the third step.
 * 1. Implement the Kafka Serde interface and create your own Serde.
 * 2. Leverage helper functions that are defined in kafka.common.serialization.Serdes class.
 * 3. The 3rd and most convenient method is to extend the Serdes class and include all your custom Serdes.
 * We prefer the 3rd approach of extending the Serdes class over implementing Serde interface because that brings all Kafka Serdes as well as the custom Serdes into a single application-level class.
 * */
public class AppSerdes extends Serdes {

    public static final class PosInvoiceSerde extends WrapperSerde<PosInvoice> {
        public PosInvoiceSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>());
        }
    }

    public static Serde<PosInvoice> PosInvoice() {
        PosInvoiceSerde serde =  new PosInvoiceSerde();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, PosInvoice.class);
        serde.configure(serdeConfigs, false);

        return serde;
    }

    public static final class DeliveryAddressSerde extends WrapperSerde<DeliveryAddress> {
        public DeliveryAddressSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>());
        }
    }

    static public Serde<DeliveryAddress> DeliveryAddress() {
        DeliveryAddressSerde serde =  new DeliveryAddressSerde();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, DeliveryAddress.class);
        serde.configure(serdeConfigs, false);

        return serde;
    }
}
