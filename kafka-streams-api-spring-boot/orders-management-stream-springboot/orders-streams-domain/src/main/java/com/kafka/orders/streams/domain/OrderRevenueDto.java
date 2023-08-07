package com.kafka.orders.streams.domain;

public record OrderRevenueDto(String locationId, OrderType orderType, TotalRevenue totalRevenue) {

}
