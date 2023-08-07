package com.kafka.orders.streams.domain;

import java.time.LocalDateTime;

public record OrdersRevenuePerStoreByWindowsDto(
        String locationId,
        TotalRevenue totalRevenue,
        OrderType orderType,
        LocalDateTime startWindow,
        LocalDateTime endWindow) {

}
