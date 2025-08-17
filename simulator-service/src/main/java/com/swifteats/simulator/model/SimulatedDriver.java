package com.swifteats.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedDriver {
    private Long id;
    private Double latitude;
    private Double longitude;
    private Double heading;
    private Double speed;
    private Double accuracy;
    
    // Destination coordinates for this driver
    private Double destLatitude;
    private Double destLongitude;
    
    // Movement parameters
    private Double stepsRemaining;
    private Double latStep;
    private Double lngStep;
}
