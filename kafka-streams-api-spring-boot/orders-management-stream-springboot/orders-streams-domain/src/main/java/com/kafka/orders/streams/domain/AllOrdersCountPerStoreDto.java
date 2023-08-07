package com.kafka.orders.streams.domain;

public record AllOrdersCountPerStoreDto(
        String locationId,
        Long orderCount,
        OrderType orderType) {

}
