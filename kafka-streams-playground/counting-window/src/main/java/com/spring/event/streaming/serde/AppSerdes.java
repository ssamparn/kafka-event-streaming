package com.spring.event.streaming.serde;

import com.spring.event.streaming.generated.SimpleInvoice;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.HashMap;
import java.util.Map;

/* *
 * Factory class for Serdes
 * */
public class AppSerdes extends Serdes {

    static final class PosInvoiceSerde extends WrapperSerde<SimpleInvoice> {
        PosInvoiceSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>());
        }
    }

    public static Serde<SimpleInvoice> SimpleInvoice() {
        PosInvoiceSerde serde = new PosInvoiceSerde();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, SimpleInvoice.class);

        serde.configure(serdeConfigs, false);
        return serde;
    }
}
