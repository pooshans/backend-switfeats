package com.swifteats.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields
public class RestaurantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String description;
    private String address;
    private String cuisine;
    private String phoneNumber;
    private Float rating;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Double deliveryFee;
    private Integer estimatedDeliveryTime;
    private String logoUrl;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("isOpen")
    private boolean isOpen; // Calculated field
}
