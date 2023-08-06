package com.kafka.orders.streams.domains;

import java.math.BigDecimal;
public record Revenue(String locationId,
                      BigDecimal finalAmount) {
}
