package com.coffee.order.util;

import com.coffee.order.domain.generated.Address;
import com.coffee.order.domain.generated.CoffeeOrder;
import com.coffee.order.domain.generated.OrderId;
import com.coffee.order.domain.generated.OrderLineItem;
import com.coffee.order.domain.generated.Size;
import com.coffee.order.domain.generated.Store;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class CoffeeOrderUtil {

    public static CoffeeOrder buildNewCoffeeOrder() {
        return CoffeeOrder.newBuilder()
                .setId(randomId())
                .setName("Sashank Samantray")
                .setStore(generateStore())
                .setOrderLineItems(generateOrderLineItems())
                .setOrderedTime(Instant.now())
                .build();
    }

    private static List<OrderLineItem> generateOrderLineItems() {
        OrderLineItem orderLineItem = OrderLineItem.newBuilder()
                .setName("Caffe Latte")
                .setQuantity(1)
                .setSize(Size.MEDIUM)
                .setCost(BigDecimal.valueOf(3.99))
                .build();

        return List.of(orderLineItem);
    }

    private static Store generateStore(){
        return  Store.newBuilder()
                .setId(randomId())
                .setAddress(buildAddress())
                .build();
    }

    private static Address buildAddress() {
        return Address.newBuilder()
                .setAddressLine1("1234 Address Line 1")
                .setCity("Chicago")
                .setStateProvince("IL")
                .setCountry("USA")
                .setZip("12345")
                .build();
    }

    public static int randomId(){
        Random random = new Random();
        return random.nextInt(1000);
    }
}
