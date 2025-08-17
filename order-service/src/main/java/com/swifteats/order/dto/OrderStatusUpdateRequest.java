package com.swifteats.order.dto;

import com.swifteats.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private OrderStatus status;

    private String notes;

    private String updatedBy;
}
