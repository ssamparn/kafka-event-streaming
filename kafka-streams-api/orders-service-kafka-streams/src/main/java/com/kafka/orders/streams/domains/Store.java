package com.kafka.orders.streams.domains;

public record Store(String locationId,
                    Address address,
                    String contactNum) {
}
