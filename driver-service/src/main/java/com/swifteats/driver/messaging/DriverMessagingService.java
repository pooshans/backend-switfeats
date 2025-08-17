package com.swifteats.driver.messaging;

import com.swifteats.driver.config.RabbitMQConfig;
import com.swifteats.driver.dto.LocationDTO;
import com.swifteats.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverMessagingService {

    private final RabbitTemplate rabbitTemplate;
    private final DriverService driverService;

    @RabbitListener(queues = RabbitMQConfig.DRIVER_ASSIGNMENT_QUEUE)
    public void handleDriverAssignment(Map<String, Object> assignment) {
        try {
            log.info("Received driver assignment: {}", assignment);
            Long driverId = ((Number) assignment.get("driverId")).longValue();
            Long orderId = ((Number) assignment.get("orderId")).longValue();

            // Process driver assignment
            // Send confirmation of assignment back to order service
            Map<String, Object> response = Map.of(
                    "orderId", orderId,
                    "driverId", driverId,
                    "status", "ASSIGNED",
                    "timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_STATUS_UPDATE_KEY,
                    response);

        } catch (Exception e) {
            log.error("Error processing driver assignment", e);
        }
    }

    public void publishDriverLocation(LocationDTO locationDTO, Long driverId) {
        try {
            Map<String, Object> message = Map.of(
                    "driverId", driverId,
                    "latitude", locationDTO.getLatitude(),
                    "longitude", locationDTO.getLongitude(),
                    "timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DRIVER_EXCHANGE,
                    RabbitMQConfig.DRIVER_LOCATION_KEY,
                    message);

            log.debug("Published driver location for driver {}: lat={}, lng={}",
                    driverId, locationDTO.getLatitude(), locationDTO.getLongitude());

        } catch (Exception e) {
            log.error("Failed to publish driver location", e);
        }
    }

    public void sendOrderStatusUpdate(Long orderId, Long driverId, String status) {
        try {
            Map<String, Object> message = Map.of(
                    "orderId", orderId,
                    "driverId", driverId,
                    "status", status,
                    "timestamp", System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_STATUS_UPDATE_KEY,
                    message);

            log.info("Sent order status update: orderId={}, status={}", orderId, status);

        } catch (Exception e) {
            log.error("Failed to send order status update", e);
        }
    }
}
