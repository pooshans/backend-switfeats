package com.swifteats.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private UUID id;
    private UUID menuItemId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String specialInstructions;
    private BigDecimal subtotal;
}
