package com.kafka.orders.streams.domains;

public record TotalRevenueWithAddress(TotalRevenue totalRevenue,
                                      Store store) {
}
