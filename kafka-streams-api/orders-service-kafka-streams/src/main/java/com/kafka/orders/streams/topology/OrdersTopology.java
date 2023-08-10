package com.kafka.orders.streams.topology;

import com.kafka.orders.streams.domains.Order;
import com.kafka.orders.streams.domains.OrderType;
import com.kafka.orders.streams.domains.Revenue;
import com.kafka.orders.streams.domains.TotalRevenue;
import com.kafka.orders.streams.serdes.SerdesFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.GENERAL_ORDERS_COUNT_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.GENERAL_ORDERS_REVENUE_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.GENERAL_ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.RESTAURANT_ORDERS_COUNT_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.RESTAURANT_ORDERS_REVENUE_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.RESTAURANT_ORDERS_TOPIC;

@Slf4j
public class OrdersTopology {

    // Business Requirement 1: Split and branch the general and restaurant orders and publish them into different topics.
    public static Topology buildSplitGeneralRestaurantOrdersTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
        orderKStream.print(Printed.<String, Order>toSysOut().withLabel("orders@source"));

        orderKStream
                .split(Named.as("General-Restaurant-Order-Branched"))
                .branch(getOrderPredicate(OrderType.GENERAL), Branched.withConsumer(generalOrdersStream -> {
                    generalOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("generalOrders@source"));
                    generalOrdersStream.to(GENERAL_ORDERS_TOPIC, Produced.with(Serdes.String(), SerdesFactory.orderSerde()));
                }))
                .branch(getOrderPredicate(OrderType.RESTAURANT), Branched.withConsumer(restaurantOrdersStream -> {
                    restaurantOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("restaurantOrders@source"));
                    restaurantOrdersStream.to(RESTAURANT_ORDERS_TOPIC, Produced.with(Serdes.String(), SerdesFactory.orderSerde()));
                }));
        return streamsBuilder.build();
    }

    private static Predicate<String, Order> getOrderPredicate(OrderType orderType) {
        return (key, order) -> order.orderType() == orderType;
    }

    // Business Requirement 2: Publish the transaction amount of general and restaurant orders, publish them into different topics after transforming the Order type to Revenue type.

    public static Topology buildTransformingOrderToRevenueTypeTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Order> orderKStream = streamsBuilder
                .stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
                // we can use groupByKey() and selectKey() interchangeably.
                // selectKey() is an expensive operation. It will trigger the repartition of records.
//                .selectKey((key, value) -> value.locationId());

        orderKStream.print(Printed.<String, Order>toSysOut().withLabel("orders@source"));

        orderKStream
                .split(Named.as("General-Restaurant-Order-Branched"))
                .branch(getOrderPredicate(OrderType.GENERAL), Branched.withConsumer(generalOrdersStream -> {
                    generalOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("generalOrders@source"));
                    generalOrdersStream
                            .mapValues((readOnlyKey, order) -> new Revenue(order.locationId(), order.finalAmount()))
                            .to(GENERAL_ORDERS_TOPIC, Produced.with(Serdes.String(), SerdesFactory.revenueSerde()));
                }))
                .branch(getOrderPredicate(OrderType.RESTAURANT), Branched.withConsumer(restaurantOrdersStream -> {
                    restaurantOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("restaurantOrders@source"));
                    restaurantOrdersStream
                            .mapValues((readOnlyKey, order) -> new Revenue(order.locationId(), order.finalAmount()))
                            .to(RESTAURANT_ORDERS_TOPIC, Produced.with(Serdes.String(), SerdesFactory.revenueSerde()));
                }));
        return streamsBuilder.build();
    }

    // Business Requirement 3: Split and branch the general and restaurant orders and count the total number of orders.

    public static Topology buildAggregateOrdersByCountTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
        orderKStream.print(Printed.<String, Order>toSysOut().withLabel("orders@source"));

        orderKStream
                .split(Named.as("General-Restaurant-Order-Branched"))
                .branch(getOrderPredicate(OrderType.GENERAL), Branched.withConsumer(generalOrdersStream -> {
                    generalOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("generalOrders@source"));

                    // group the order based on locationId.
                    KTable<String, Long> generalOrdersCountKTable = generalOrdersStream
                            .map((key, value) -> KeyValue.pair(value.locationId(), value))
                            .groupByKey(Grouped.with(Serdes.String(), SerdesFactory.orderSerde()))
                            .count(Named.as(GENERAL_ORDERS_COUNT_TOPIC), Materialized.as(GENERAL_ORDERS_COUNT_TOPIC));

                    generalOrdersCountKTable.toStream()
                                    .print(Printed.<String, Long>toSysOut().withLabel(GENERAL_ORDERS_COUNT_TOPIC));
                }))
                .branch(getOrderPredicate(OrderType.RESTAURANT), Branched.withConsumer(restaurantOrdersStream -> {
                    restaurantOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("restaurantOrders@source"));
                    KTable<String, Long> restaurantOrdersCountKTable =  restaurantOrdersStream
                            .map((key, value) -> KeyValue.pair(value.locationId(), value))
                            .groupByKey(Grouped.with(Serdes.String(), SerdesFactory.orderSerde()))
                            .count(Named.as(RESTAURANT_ORDERS_COUNT_TOPIC), Materialized.as(RESTAURANT_ORDERS_COUNT_TOPIC));
                    restaurantOrdersCountKTable.toStream()
                            .print(Printed.<String, Long>toSysOut().withLabel(RESTAURANT_ORDERS_COUNT_TOPIC));
                }));
        return streamsBuilder.build();
    }

    // Business Requirement 4: Split and branch the general and restaurant orders and count the total revenue made from the orders.

    public static Topology buildAggregateOrdersByCountingTotalRevenueMadeFromOrdersTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
        orderKStream.print(Printed.<String, Order>toSysOut().withLabel("orders@source"));

        Initializer<TotalRevenue> totalRevenueInitializer = TotalRevenue::new;
        Aggregator<String, Order, TotalRevenue> aggregator = (key, value, totalRevenueAggregate) -> totalRevenueAggregate.updateRunningRevenue(key, value);

        orderKStream
                .split(Named.as("General-Restaurant-Order-Branched"))
                .branch(getOrderPredicate(OrderType.GENERAL), Branched.withConsumer(generalOrdersStream -> {
                    generalOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("generalOrders@source"));

                    // group the order based on locationId.
                    KTable<String, TotalRevenue> generalOrdersCountKTable = generalOrdersStream
                            .map((key, value) -> KeyValue.pair(value.locationId(), value))
                            .groupByKey(Grouped.with(Serdes.String(), SerdesFactory.orderSerde()))
                            .aggregate(totalRevenueInitializer,
                                    aggregator,
                                    Materialized.<String, TotalRevenue, KeyValueStore<Bytes, byte[]>>as(GENERAL_ORDERS_REVENUE_TOPIC)
                                            .withKeySerde(Serdes.String())
                                            .withValueSerde(SerdesFactory.totalRevenueSerde())
                            );

                    generalOrdersCountKTable.toStream()
                            .print(Printed.<String, TotalRevenue>toSysOut().withLabel(GENERAL_ORDERS_REVENUE_TOPIC));
                }))
                .branch(getOrderPredicate(OrderType.RESTAURANT), Branched.withConsumer(restaurantOrdersStream -> {
                    restaurantOrdersStream.print(Printed.<String, Order>toSysOut().withLabel("restaurantOrders@source"));

                    // group the order based on locationId.
                    KTable<String, TotalRevenue> restaaurntOrdersCountKTable = restaurantOrdersStream
                            .map((key, value) -> KeyValue.pair(value.locationId(), value))
                            .groupByKey(Grouped.with(Serdes.String(), SerdesFactory.orderSerde()))
                            .aggregate(totalRevenueInitializer,
                                    aggregator,
                                    Materialized.<String, TotalRevenue, KeyValueStore<Bytes, byte[]>>as(RESTAURANT_ORDERS_REVENUE_TOPIC)
                                            .withKeySerde(Serdes.String())
                                            .withValueSerde(SerdesFactory.totalRevenueSerde())
                            );

                    restaaurntOrdersCountKTable.toStream()
                            .print(Printed.<String, TotalRevenue>toSysOut().withLabel(RESTAURANT_ORDERS_REVENUE_TOPIC));
                }));

        return streamsBuilder.build();
    }



}
