package com.kafka.order.controller;

import com.kafka.order.dto.CoffeeOrderDto;
import com.kafka.order.service.CoffeeOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/coffee_orders")
public class CoffeeOrderController {

    private final CoffeeOrderService coffeeOrderService;

    @PostMapping
    public ResponseEntity<CoffeeOrderDto> newOrder(@RequestBody CoffeeOrderDto coffeeOrderDto) {
        CoffeeOrderDto createdOrder = coffeeOrderService.createNewOrder(coffeeOrderDto);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
}
