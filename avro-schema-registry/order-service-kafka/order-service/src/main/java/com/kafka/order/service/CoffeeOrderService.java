package com.kafka.order.service;

import com.coffee.order.domain.generated.Address;
import com.coffee.order.domain.generated.CoffeeOrder;
import com.coffee.order.domain.generated.OrderLineItem;
import com.coffee.order.domain.generated.Store;
import com.kafka.order.dto.CoffeeOrderDto;
import com.kafka.order.dto.StoreDto;
import com.kafka.order.producer.CoffeeOrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoffeeOrderService {

    private final CoffeeOrderProducer producer;

    public CoffeeOrderDto createNewOrder(CoffeeOrderDto coffeeOrderDto) {
        CoffeeOrder coffeeOrderAvroMessage = this.toCoffeeOrderSchema(coffeeOrderDto);
        coffeeOrderDto.setId(coffeeOrderAvroMessage.getId().toString());
        producer.sendMessage(coffeeOrderAvroMessage);

        return coffeeOrderDto;
    }

    private CoffeeOrder toCoffeeOrderSchema(CoffeeOrderDto coffeeOrderDto) {
        Store store = getStore(coffeeOrderDto);

        var orderLineItems = buildOrderLineItems(coffeeOrderDto);

        return CoffeeOrder.newBuilder()
                .setId(UUID.randomUUID())
                .setName(coffeeOrderDto.getName())
                .setNickName(coffeeOrderDto.getNickName())
                .setStore(store)
                .setOrderLineItems(orderLineItems)
                .setStatus(coffeeOrderDto.getStatus())
                .setOrderedTime(Instant.now())
                .setPickUp(coffeeOrderDto.getPickUp())
                .setStatus(coffeeOrderDto.getStatus())
                .build();
    }

    private List<OrderLineItem> buildOrderLineItems(CoffeeOrderDto coffeeOrderDto) {
        return coffeeOrderDto.getOrderLineItems()
            .stream().map(orderLineItem ->
                new OrderLineItem(
                        orderLineItem.getName(),
                        orderLineItem.getSize(),
                        orderLineItem.getQuantity(),
                        orderLineItem.getCost()
                )
            )
            .collect(Collectors.toList());
    }

    private Store getStore(CoffeeOrderDto coffeeOrderDto) {
        StoreDto storeDto = coffeeOrderDto.getStore();
        return new Store(storeDto.getStoreId(),
            new Address(storeDto.getAddress().getAddressLine1(),
                storeDto.getAddress().getCity(),
                storeDto.getAddress().getState(),
                storeDto.getAddress().getCountry(),
                storeDto.getAddress().getZip()
            ));
    }
}
