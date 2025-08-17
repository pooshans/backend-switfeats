package com.swifteats.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdate {
    private Long driverId; // Changed back to Long as expected by the Driver Service
    private Double latitude;
    private Double longitude;

    // Optional fields that may be used by the Driver Service
    private Double heading;
    private Double speed;
    private Double accuracy;

    // Constructor for creating location updates that match the expected API format
    public LocationUpdate(Long driverId, Double latitude, Double longitude) {
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
