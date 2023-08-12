package com.kafka.ordersstreamsapp.topology;

import com.kafka.orders.streams.domain.Order;
import com.kafka.orders.streams.domain.OrderType;
import com.kafka.orders.streams.domain.Store;
import com.kafka.orders.streams.domain.TotalCountWithAddress;
import com.kafka.orders.streams.domain.TotalRevenue;
import com.kafka.orders.streams.domain.TotalRevenueWithAddress;
import com.kafka.ordersstreamsapp.util.OrderTimeStampExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.BiFunction;

import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.*;

@Slf4j
@Component
public class OrdersTopology {

    @Autowired
    public void process(StreamsBuilder streamsBuilder) {
        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Order.class))
                .withTimestampExtractor(new OrderTimeStampExtractor()))
                        .selectKey((key, value) -> value.locationId());

        orderKStream.print(Printed.<String, Order>toSysOut().withLabel("orders@source"));

        KTable<String, Store> storesKTable = streamsBuilder.table(STORES_TOPIC, Consumed.with(Serdes.String(), new JsonSerde<>(Store.class)));
        storesKTable.toStream().print(Printed.<String, Store>toSysOut().withLabel("storetable@source"));

        orderKStream
                .split(Named.as("general-restaurant-order-stream"))
                .branch(getOrderPredicate(OrderType.GENERAL), Branched.withConsumer(generalOrdersStream -> {
                    generalOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("generalOrders@source"));
                        aggregateOrdersByCount(generalOrdersStream, GENERAL_ORDERS_COUNT_TOPIC, storesKTable);
                        aggregateOrdersCountByTimeWindows(generalOrdersStream, GENERAL_ORDERS_COUNT_WINDOWS_TOPIC, storesKTable);
                        aggregateOrdersByRevenue(generalOrdersStream, GENERAL_ORDERS_REVENUE_TOPIC, storesKTable);
                        aggregateOrdersRevenueByTimeWindows(generalOrdersStream, GENERAL_ORDERS_REVENUE_WINDOWS_TOPIC, storesKTable);
                }))
                .branch(getOrderPredicate(OrderType.RESTAURANT), Branched.withConsumer(restaurantOrdersStream -> {
                    restaurantOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("restaurantOrders@source"));
                        aggregateOrdersByCount(restaurantOrdersStream, RESTAURANT_ORDERS_COUNT_TOPIC, storesKTable);
                        aggregateOrdersCountByTimeWindows(restaurantOrdersStream, RESTAURANT_ORDERS_COUNT_WINDOWS_TOPIC, storesKTable);
                        aggregateOrdersByRevenue(restaurantOrdersStream, RESTAURANT_ORDERS_REVENUE_TOPIC, storesKTable);
                        aggregateOrdersRevenueByTimeWindows(restaurantOrdersStream, RESTAURANT_ORDERS_REVENUE_WINDOWS_TOPIC, storesKTable);
                }));
    }

    private static Predicate<String, Order> getOrderPredicate(OrderType orderType) {
        return (key, order) -> order.orderType() == orderType;
    }

    private static void aggregateOrdersByCount(KStream<String, Order> ordersStream, String storeName, KTable<String, Store> storeKTable) {
        KTable<String, Long> ordersCountPerStoreKTable = ordersStream
//                .map((key, value) -> KeyValue.pair(value.locationId(), value))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Order.class)))
                .count(Named.as(storeName), Materialized.as(storeName));
        ordersCountPerStoreKTable.toStream().print(Printed.<String,Long>toSysOut().withLabel(storeName));

        ValueJoiner<Long, Store, TotalCountWithAddress> valueJoiner = TotalCountWithAddress::new;

        KTable<String, TotalCountWithAddress> joinedKTable = ordersCountPerStoreKTable.join(storeKTable, valueJoiner);
        joinedKTable.toStream().print(Printed.<String, TotalCountWithAddress>toSysOut().withLabel(storeName + "-bystore"));
    }

    private void aggregateOrdersCountByTimeWindows(KStream<String, Order> ordersStream, String storeName, KTable<String, Store> storesKTable) {

        Duration windowSize = Duration.ofSeconds(60);
        TimeWindows hoppingWindow = TimeWindows.ofSizeWithNoGrace(windowSize);

        KTable<Windowed<String>, Long> ordersCountPerStore = ordersStream
                .map((key, value) -> KeyValue.pair(value.locationId(), value))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Order.class)))
                .windowedBy(hoppingWindow)
                .count(Named.as(storeName), Materialized.as(storeName))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()));

        ordersCountPerStore
                .toStream()
                .peek(((key, value) -> {
                    log.info(" {} : tumblingWindow : key : {}, value : {}",storeName, key, value);
                    printLocalDateTimes(key, value);
                }))
                .print(Printed.<Windowed<String>, Long>toSysOut().withLabel(storeName));
    }

    private void aggregateOrdersByRevenue(KStream<String, Order> ordersStream, String storeName, KTable<String, Store> storesKTable) {
        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;

        Aggregator<String, Order, TotalRevenue> aggregator = (key, order, totalRevenue) -> totalRevenue.updateRunningRevenue(key, order);

        KTable<String, TotalRevenue> revenueTable = ordersStream
                .map((key, value) -> KeyValue.pair(value.locationId(), value))
                .groupByKey(Grouped.with(Serdes.String(),new JsonSerde<>(Order.class)))
                .aggregate(totalRevenueInitializer, aggregator,
                        Materialized.<String, TotalRevenue, KeyValueStore<Bytes, byte[]>>as(storeName)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new JsonSerde<>(TotalRevenue.class)));

        revenueTable.toStream().print(Printed.<String,TotalRevenue>toSysOut().withLabel(storeName));

        ValueJoiner<TotalRevenue, Store, TotalRevenueWithAddress> valueJoiner = TotalRevenueWithAddress::new;
        KTable<String, TotalRevenueWithAddress> revenueWithStoreTable = revenueTable.leftJoin(storesKTable, valueJoiner);

        revenueWithStoreTable.toStream().print(Printed.<String,TotalRevenueWithAddress>toSysOut().withLabel(storeName + "-bystore"));
    }

    private void aggregateOrdersRevenueByTimeWindows(KStream<String, Order> ordersKStream, String storeName, KTable<String, Store> storesKTable) {
        var windowSize = 15;
        Duration windowSizeDuration = Duration.ofSeconds(windowSize);
        TimeWindows timeWindows = TimeWindows.ofSizeWithNoGrace(windowSizeDuration);

        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;
        Aggregator<String, Order, TotalRevenue> aggregator = (key,order, totalRevenue) -> totalRevenue.updateRunningRevenue(key, order);

        var revenueTable = ordersKStream
                .map((key, value) -> KeyValue.pair(value.locationId(), value))
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Order.class)))
                .windowedBy(timeWindows)
                .aggregate(totalRevenueInitializer, aggregator ,Materialized.<String, TotalRevenue, WindowStore<Bytes, byte[]>>as(storeName)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(TotalRevenue.class))
                );

        revenueTable
                .toStream()
                .peek(((key, value) -> {
                    log.info(" {} : tumblingWindow : key : {}, value : {}", storeName, key, value);
                    printLocalDateTimes(key, value);
                }))
                .print(Printed.<Windowed<String>,TotalRevenue>toSysOut().withLabel(storeName));

        ValueJoiner<TotalRevenue, Store, TotalRevenueWithAddress> valueJoiner = TotalRevenueWithAddress::new;
        Joined<String, TotalRevenue, Store> joinedParams = Joined.with(Serdes.String(), new JsonSerde<>(TotalRevenue.class), new JsonSerde<>(Store.class));

        revenueTable
                .toStream()
                .map((key, value) -> KeyValue.pair(key.key(), value))
                .join(storesKTable, valueJoiner, joinedParams)
                .print(Printed.<String, TotalRevenueWithAddress>toSysOut().withLabel(storeName + "-bystore"));
    }

    public static void printLocalDateTimes(Windowed<String> key, Object value) {
        var startTime = key.window().startTime();
        var endTime = key.window().endTime();
        log.info("startTime : {} , endTime : {}, Count : {}", startTime, endTime, value);
        LocalDateTime startLDT = LocalDateTime.ofInstant(startTime, ZoneId.of(ZoneId.SHORT_IDS.get("CST")));
        LocalDateTime endLDT = LocalDateTime.ofInstant(endTime, ZoneId.of(ZoneId.SHORT_IDS.get("CST")));
        log.info("startLDT : {} , endLDT : {}, Count : {}", startLDT, endLDT, value);
    }

}
