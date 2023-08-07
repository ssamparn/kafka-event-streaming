package com.kafka.orders.streams.domain;

import java.math.BigDecimal;
public record Revenue(String locationId, BigDecimal finalAmount) {

}
