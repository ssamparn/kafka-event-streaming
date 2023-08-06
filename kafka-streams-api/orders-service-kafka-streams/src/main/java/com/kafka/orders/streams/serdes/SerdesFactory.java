package com.kafka.orders.streams.serdes;

import com.kafka.orders.streams.domains.Order;
import com.kafka.orders.streams.domains.Revenue;
import com.kafka.orders.streams.domains.Store;
import com.kafka.orders.streams.domains.TotalRevenue;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SerdesFactory {

    public static Serde<Order> orderSerde(){
        JsonSerializer<Order> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Order> jsonDeSerializer = new JsonDeserializer<>(Order.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }

    public static Serde<Store> storeSerde() {
        JsonSerializer<Store> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Store> jsonDeSerializer = new JsonDeserializer<>(Store.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }


    public static Serde<Revenue> revenueSerde() {
        JsonSerializer<Revenue> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Revenue> jsonDeSerializer = new JsonDeserializer<>(Revenue.class);

        return Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }

    public static Serde<TotalRevenue> totalRevenueSerde() {
        JsonSerializer<TotalRevenue> jsonSerializer = new JsonSerializer<>();
        JsonDeserializer<TotalRevenue> jsonDeSerializer = new JsonDeserializer<>(TotalRevenue.class);

        return  Serdes.serdeFrom(jsonSerializer, jsonDeSerializer);
    }
}
