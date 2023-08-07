package com.kafka.orders.streams.domain;

public record OrderCountPerStoreDto(String locationId, Long orderCount) {

}
