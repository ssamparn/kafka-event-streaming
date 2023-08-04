package com.kafka.order.dto;

import com.coffee.order.domain.generated.PickUp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeOrderDto {
    private String id;
    private String name;
    private String nickName;
    private StoreDto store;

    @JsonProperty("orderLineItems")
    private List<OrderLineItemDto> orderLineItems;

    @JsonProperty("pickUp")
    private PickUp pickUp;

    private String status;
}
