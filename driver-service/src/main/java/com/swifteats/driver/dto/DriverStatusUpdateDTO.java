package com.swifteats.driver.dto;

import com.swifteats.driver.model.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatusUpdateDTO {
    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Status is required")
    private DriverStatus status;
}
