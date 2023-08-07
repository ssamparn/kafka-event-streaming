package com.kafka.orders.streams.domain;

import java.time.LocalDateTime;

public record OrdersCountPerStoreByWindowsDto(
        String locationId,
        Long orderCount,
        OrderType orderType,
        LocalDateTime startWindow,
        LocalDateTime endWindow) {

}
