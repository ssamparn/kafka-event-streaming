package com.kafka.orders.streams.topology;

import com.kafka.orders.streams.domains.Order;
import com.kafka.orders.streams.domains.OrderType;
import com.kafka.orders.streams.domains.Revenue;
import com.kafka.orders.streams.serdes.SerdesFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.GENERAL_ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.RESTAURANT_ORDERS_TOPIC;

@Slf4j
public class OrdersTopology {

    // Business Requirement 1: Split the general and restaurant orders and publish them into different topics.
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

        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
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

    public static Topology buildAggregateOrdersByCountTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, Order> orderKStream = streamsBuilder.stream(ORDERS_TOPIC, Consumed.with(Serdes.String(), SerdesFactory.orderSerde()));
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



}
