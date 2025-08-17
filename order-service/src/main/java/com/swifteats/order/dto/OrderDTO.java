package com.swifteats.order.dto;

import com.swifteats.order.domain.OrderStatus;
import com.swifteats.order.domain.PaymentMethod;
import com.swifteats.order.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private UUID id;
    private UUID userId;
    private UUID restaurantId;
    private UUID driverId;
    private String deliveryAddress;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime completedAt;
    private String specialInstructions;
    private List<OrderItemDTO> items;
}
