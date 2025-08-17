package com.swifteats.driver.websocket;

import com.swifteats.driver.dto.LocationDTO;
import com.swifteats.driver.dto.LocationUpdateDTO;
import com.swifteats.driver.messaging.DriverMessagingService;
import com.swifteats.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DriverLocationWebSocketHandler {

    private final DriverService driverService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DriverMessagingService messagingService;

    @MessageMapping("/location/update")
    public void handleLocationUpdate(@Payload LocationUpdateDTO locationUpdate) {
        try {
            log.debug("Received location update via WebSocket: {}", locationUpdate);

            // Update location in database and cache
            LocationDTO updatedLocation = driverService.updateDriverLocation(locationUpdate);

            // Broadcast to all subscribers
            messagingTemplate.convertAndSend(
                    "/topic/location/all",
                    updatedLocation);

            // Also broadcast to driver-specific channel
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + locationUpdate.getDriverId() + "/location",
                    updatedLocation);

            // Publish to message queue for other services
            messagingService.publishDriverLocation(updatedLocation, locationUpdate.getDriverId());

        } catch (Exception e) {
            log.error("Error processing WebSocket location update", e);
        }
    }
}
