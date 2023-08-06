package com.kafka.orders.streams.domains;

import java.math.BigDecimal;

public record OrderLineItem(
        String item,
        Integer count,
        BigDecimal amount) {
}
