package com.spring.event.streaming.avro.serdes;

import com.spring.event.streaming.avro.generated.HadoopRecord;
import com.spring.event.streaming.avro.generated.Notification;
import com.spring.event.streaming.avro.generated.PosInvoice;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.HashMap;
import java.util.Map;

public class AppSerdes extends Serdes {

    public static Serde<PosInvoice> PosInvoice() {
        Serde<PosInvoice> serde = new SpecificAvroSerde<>();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put("schema.registry.url", "http://localhost:8081");
        serde.configure(serdeConfigs, false);

        return serde;
    }

    public static Serde<Notification> Notification() {
        Serde<Notification> serde = new SpecificAvroSerde<>();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put("schema.registry.url", "http://localhost:8081");
        serde.configure(serdeConfigs, false);

        return serde;
    }

    public static Serde<HadoopRecord> HadoopRecord() {
        Serde<HadoopRecord> serde = new SpecificAvroSerde<>();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put("schema.registry.url", "http://localhost:8081");
        serde.configure(serdeConfigs, false);

        return serde;
    }
}
