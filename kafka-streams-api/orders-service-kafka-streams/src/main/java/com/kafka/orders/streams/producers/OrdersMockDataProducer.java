package com.kafka.orders.streams.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafka.orders.streams.domains.Order;
import com.kafka.orders.streams.domains.OrderLineItem;
import com.kafka.orders.streams.domains.OrderType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.kafka.orders.streams.utils.OrdersKafkaStreamUtil.ORDERS_TOPIC;
import static com.kafka.orders.streams.utils.ProducerUtil.publishMessageSync;
import static java.lang.Thread.sleep;

@Slf4j
public class OrdersMockDataProducer {

    public static void main(String[] args) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        publishOrders(objectMapper, buildOrders());
//        publishBulkOrders(objectMapper);
    }

    private static void publishOrders(ObjectMapper objectMapper, List<Order> orders) {
        orders
                .forEach(order -> {
                    try {
                        String orderMessages = objectMapper.writeValueAsString(order);
                        var recordMetaData = publishMessageSync(ORDERS_TOPIC, String.valueOf(order.orderId()), orderMessages);
                        log.info("Published the order message : {} ", recordMetaData);
                    } catch (JsonProcessingException e) {
                        log.error("JsonProcessingException : {} ", e.getMessage(), e);
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        log.error("Exception : {} ", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void publishBulkOrders(ObjectMapper objectMapper) throws InterruptedException {

        int count = 0;
        while (count < 100) {
            var orders = buildOrders();
            publishOrders(objectMapper, orders);
            sleep(1000);
            count++;
        }
    }

    private static List<Order> buildOrders() {
        var orderItems = List.of(
                new OrderLineItem("Bananas", 2, new BigDecimal("2.00")),
                new OrderLineItem("Iphone Charger", 1, new BigDecimal("25.00"))
        );

        var orderItemsRestaurant = List.of(
                new OrderLineItem("Pizza", 2, new BigDecimal("12.00")),
                new OrderLineItem("Coffee", 1, new BigDecimal("3.00"))
        );

        var order1 = new Order(12345, "store_1234",
                new BigDecimal("27.00"),
                OrderType.GENERAL,
                orderItems,
                LocalDateTime.now()
                //LocalDateTime.now(ZoneId.of("UTC"))
        );

        var order2 = new Order(54321, "store_1234",
                new BigDecimal("15.00"),
                OrderType.RESTAURANT,
                orderItemsRestaurant,
                LocalDateTime.now()
                //LocalDateTime.now(ZoneId.of("UTC"))
        );

        var order3 = new Order(12345, "store_4567",
                new BigDecimal("27.00"),
                OrderType.GENERAL,
                orderItems,
                LocalDateTime.now()
                //LocalDateTime.now(ZoneId.of("UTC"))
        );

        var order4 = new Order(12345, "store_4567",
                new BigDecimal("27.00"),
                OrderType.RESTAURANT,
                orderItems,
                LocalDateTime.now()
                //LocalDateTime.now(ZoneId.of("UTC"))
        );

        return List.of(
                order1,
                order2,
                order3,
                order4
        );
    }
}
