package com.kafka.ordersstreamsapp.service;

import com.kafka.orders.streams.domain.AllOrdersCountPerStoreDto;
import com.kafka.orders.streams.domain.OrderCountPerStoreDto;
import com.kafka.orders.streams.domain.OrderRevenueDto;
import com.kafka.orders.streams.domain.OrderType;
import com.kafka.orders.streams.domain.TotalRevenue;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.GENERAL_ORDERS_COUNT_TOPIC;
import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.GENERAL_ORDERS_REVENUE_TOPIC;
import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.GENERAL_ORDERS_TOPIC;
import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.RESTAURANT_ORDERS_COUNT_TOPIC;
import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.RESTAURANT_ORDERS_REVENUE_TOPIC;
import static com.kafka.ordersstreamsapp.config.OrdersStreamsConfig.RESTAURANT_ORDERS_TOPIC;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderStoreService orderStoreService;

    public List<OrderCountPerStoreDto> getOrdersCount(String orderType) {
        ReadOnlyKeyValueStore<String, Long> orderStore = this.getOrderStore(orderType);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(orderStore.all(), 0), false)
                .map(keyValue -> new OrderCountPerStoreDto(keyValue.key, keyValue.value))
                .collect(Collectors.toList());
    }

    public OrderCountPerStoreDto getOrdersCountByLocationId(String orderType, String locationId) {
        ReadOnlyKeyValueStore<String, Long> orderStore = this.getOrderStore(orderType);
        Long orderCount = orderStore.get(locationId);

        if (orderStore != null) {
            return new OrderCountPerStoreDto(locationId, orderCount);
        }
        return null;
    }

    public List<AllOrdersCountPerStoreDto> getAllOrdersCount() {

        BiFunction<OrderCountPerStoreDto, OrderType, AllOrdersCountPerStoreDto> mapper =
                (orderCountPerStoreDto, orderType) -> new AllOrdersCountPerStoreDto(orderCountPerStoreDto.locationId(), orderCountPerStoreDto.orderCount(), orderType);

        List<AllOrdersCountPerStoreDto> generalOrders = this.getOrdersCount(GENERAL_ORDERS_TOPIC)
                .stream()
                .map(orderCountPerStoreDto -> mapper.apply(orderCountPerStoreDto, OrderType.GENERAL))
                .toList();

        List<AllOrdersCountPerStoreDto> restaurantOrders = this.getOrdersCount(RESTAURANT_ORDERS_TOPIC)
                .stream()
                .map(orderCountPerStoreDto -> mapper.apply(orderCountPerStoreDto, OrderType.RESTAURANT))
                .toList();

        return Stream.of(generalOrders, restaurantOrders)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<OrderRevenueDto> getRevenueByOrderType(String orderType) {
        ReadOnlyKeyValueStore<String, TotalRevenue> revenueStore = this.getRevenueStore(orderType);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(revenueStore.all(), 0), false)
                .map(keyValue -> new OrderRevenueDto(keyValue.key, mapOrderType(orderType), keyValue.value))
                .collect(Collectors.toList());
    }

    public OrderRevenueDto getRevenueByOrderTypeAndLocationId(String orderType, String locationId) {
        ReadOnlyKeyValueStore<String, TotalRevenue> revenueStore = this.getRevenueStore(orderType);
        TotalRevenue totalRevenue = revenueStore.get(locationId);

        if (totalRevenue != null) {
            return new OrderRevenueDto(locationId, mapOrderType(orderType), totalRevenue);
        }
        return null;
    }


    public static OrderType mapOrderType(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_TOPIC -> OrderType.GENERAL;
            case RESTAURANT_ORDERS_TOPIC -> OrderType.RESTAURANT;
            default -> throw new IllegalStateException("Not a Valid Option");
        };
    }

    public ReadOnlyKeyValueStore<String, Long> getOrderStore(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_TOPIC -> orderStoreService.ordersCountStore(GENERAL_ORDERS_COUNT_TOPIC);
            case RESTAURANT_ORDERS_TOPIC -> orderStoreService.ordersCountStore(RESTAURANT_ORDERS_COUNT_TOPIC);
            default -> throw new IllegalStateException("Not a valid option");
        };
    }

    public ReadOnlyKeyValueStore<String, TotalRevenue> getRevenueStore(String orderType) {
        return switch (orderType) {
            case GENERAL_ORDERS_TOPIC -> orderStoreService.ordersRevenueStore(GENERAL_ORDERS_REVENUE_TOPIC);
            case RESTAURANT_ORDERS_TOPIC -> orderStoreService.ordersRevenueStore(RESTAURANT_ORDERS_REVENUE_TOPIC);
            default -> throw new IllegalStateException("Not a valid option");
        };
    }
}
