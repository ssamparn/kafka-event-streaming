package com.kafka.ordersstreamsapp.controller;

import com.kafka.orders.streams.domain.AllOrdersCountPerStoreDto;
import com.kafka.orders.streams.domain.OrderCountPerStoreDto;
import com.kafka.orders.streams.domain.OrderRevenueDto;
import com.kafka.ordersstreamsapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrdersController {

    private final OrderService orderService;

    @GetMapping("/count/{orderType}")
    public ResponseEntity<?> ordersCount(@PathVariable("orderType") String orderType, @RequestParam(value = "locationId", required = false) String locationId) {
        if (StringUtils.hasLength(locationId)) {
            return ResponseEntity.ok(orderService.getOrdersCountByLocationId(orderType, locationId));
        } else {
            return ResponseEntity.ok(orderService.getOrdersCount(orderType));
        }
    }

    @GetMapping("/count-all-orders")
    public ResponseEntity<List<AllOrdersCountPerStoreDto>> allOrdersCount() {
        return ResponseEntity.ok(orderService.getAllOrdersCount());
    }

    @GetMapping("/revenue/{orderType}")
    public ResponseEntity<?> revenueByOrderType(@PathVariable("orderType") String orderType, @RequestParam(value = "locationId", required = false) String locationId) {
        if (StringUtils.hasLength(locationId)) {
            return ResponseEntity.ok(orderService.getRevenueByOrderTypeAndLocationId(orderType, locationId));
        } else {
            return ResponseEntity.ok(orderService.getRevenueByOrderType(orderType));
        }
    }

}
