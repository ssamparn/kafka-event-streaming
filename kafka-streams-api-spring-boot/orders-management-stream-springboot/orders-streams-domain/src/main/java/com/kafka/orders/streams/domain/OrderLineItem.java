package com.kafka.orders.streams.domain;

import java.math.BigDecimal;

public record OrderLineItem(String item, Integer count, BigDecimal amount) {

}
