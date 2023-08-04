package com.kafka.order.dto;

import com.coffee.order.domain.generated.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemDto {
    private String name;

    private Size size;

    private Integer quantity;

    private BigDecimal cost;
}
