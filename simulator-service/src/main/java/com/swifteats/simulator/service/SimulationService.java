package com.swifteats.simulator.service;

import com.swifteats.simulator.model.LocationUpdate;
import com.swifteats.simulator.model.SimulatedDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {

    private final WebClient.Builder webClientBuilder;
    private final Map<Long, SimulatedDriver> driversMap = new ConcurrentHashMap<>();

    @Value("${simulator.driver.count:10000}")
    private int driverCount;

    @Value("${simulator.api.url:http://api-gateway:8080}")
    private String apiUrl; // Set to API Gateway URL to route requests through the gateway

    @Value("${simulator.update.interval:500}")
    private long updateIntervalMs;

    // San Francisco coordinates for simulation
    private static final double SF_LAT_MIN = 37.7;
    private static final double SF_LAT_MAX = 37.8;
    private static final double SF_LNG_MIN = -122.5;
    private static final double SF_LNG_MAX = -122.4;

    @PostConstruct
    public void initializeDrivers() {
        log.info("Initializing {} simulated drivers", driverCount);

        IntStream.rangeClosed(1, driverCount).forEach(i -> {
            double startLat = randomBetween(SF_LAT_MIN, SF_LAT_MAX);
            double startLng = randomBetween(SF_LNG_MIN, SF_LNG_MAX);
            double destLat = randomBetween(SF_LAT_MIN, SF_LAT_MAX);
            double destLng = randomBetween(SF_LNG_MIN, SF_LNG_MAX);

            // Calculate steps and increments for smooth movement
            int steps = ThreadLocalRandom.current().nextInt(50, 150);
            double latStep = (destLat - startLat) / steps;
            double lngStep = (destLng - startLng) / steps;

            // Calculate heading (direction in degrees)
            double deltaLng = destLng - startLng;
            double y = Math.sin(deltaLng) * Math.cos(destLat);
            double x = Math.cos(startLat) * Math.sin(destLat)
                    - Math.sin(startLat) * Math.cos(destLat) * Math.cos(deltaLng);
            double heading = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

            // Speed in km/h, randomly assigned
            double speed = randomBetween(15, 60);

            SimulatedDriver driver = SimulatedDriver.builder()
                    .id((long) i)
                    .latitude(startLat)
                    .longitude(startLng)
                    .destLatitude(destLat)
                    .destLongitude(destLng)
                    .heading(heading)
                    .speed(speed)
                    .accuracy(randomBetween(3, 10))
                    .stepsRemaining((double) steps)
                    .latStep(latStep)
                    .lngStep(lngStep)
                    .build();

            driversMap.put((long) i, driver);
        });

        log.info("Finished initializing {} drivers", driversMap.size());
    }

    @Scheduled(fixedDelayString = "${simulator.update.interval:500}")
    public void updateDriverLocations() {
        log.debug("Updating locations for {} drivers", driversMap.size());

        List<LocationUpdate> batchUpdates = new ArrayList<>();

        driversMap.forEach((id, driver) -> {
            // Move the driver according to the calculated steps
            if (driver.getStepsRemaining() > 0) {
                driver.setLatitude(driver.getLatitude() + driver.getLatStep());
                driver.setLongitude(driver.getLongitude() + driver.getLngStep());
                driver.setStepsRemaining(driver.getStepsRemaining() - 1);
            } else {
                // Driver reached destination, set new destination
                double newDestLat = randomBetween(SF_LAT_MIN, SF_LAT_MAX);
                double newDestLng = randomBetween(SF_LNG_MIN, SF_LNG_MAX);

                // Calculate new steps and increments
                int steps = ThreadLocalRandom.current().nextInt(50, 150);
                double latStep = (newDestLat - driver.getLatitude()) / steps;
                double lngStep = (newDestLng - driver.getLongitude()) / steps;

                // Update driver properties
                driver.setDestLatitude(newDestLat);
                driver.setDestLongitude(newDestLng);
                driver.setStepsRemaining((double) steps);
                driver.setLatStep(latStep);
                driver.setLngStep(lngStep);

                // Recalculate heading
                double deltaLng = newDestLng - driver.getLongitude();
                double y = Math.sin(deltaLng) * Math.cos(newDestLat);
                double x = Math.cos(driver.getLatitude()) * Math.sin(newDestLat) -
                        Math.sin(driver.getLatitude()) * Math.cos(newDestLat) * Math.cos(deltaLng);
                double heading = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

                driver.setHeading(heading);
                driver.setSpeed(randomBetween(15, 60));
            }

            // Create location update object that matches the expected API format
            // Use the numeric ID directly as expected by the Driver Service
            LocationUpdate update = new LocationUpdate(
                    id,
                    driver.getLatitude(),
                    driver.getLongitude());

            // Add optional fields that may or may not be used by the API
            update.setHeading(driver.getHeading());
            update.setSpeed(driver.getSpeed());
            update.setAccuracy(driver.getAccuracy());

            batchUpdates.add(update);

            // Send updates in batches of 100 to avoid overwhelming the system
            if (batchUpdates.size() >= 100) {
                sendBatchUpdates(new ArrayList<>(batchUpdates));
                batchUpdates.clear();
            }
        });

        // Send any remaining updates
        if (!batchUpdates.isEmpty()) {
            sendBatchUpdates(batchUpdates);
        }
    }

    private void sendBatchUpdates(List<LocationUpdate> updates) {
        WebClient client = webClientBuilder.baseUrl(apiUrl).build();

        // Send location updates through the API Gateway which routes to the Driver
        // Service
        client.post()
                .uri("/api/v1/drivers/location/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Successfully sent batch of {} location updates", updates.size()))
                .doOnError(error -> {
                    log.error("Error sending batch updates: {}", error.getMessage());

                    // If batch update fails, try sending individually
                    if (updates.size() > 1 && error.getMessage().contains("400 Bad Request")) {
                        log.info("Batch update failed, trying individual updates for each driver");
                        updates.forEach(this::sendSingleUpdate);
                    }
                })
                .subscribe();
    }

    private void sendSingleUpdate(LocationUpdate update) {
        WebClient client = webClientBuilder.baseUrl(apiUrl).build();

        // Send individual location update through the API Gateway
        client.post()
                .uri("/api/v1/drivers/location")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(
                        response -> log.info("Successfully sent location update for driver {}", update.getDriverId()))
                .doOnError(error -> log.error("Error sending location update for driver {}: {}",
                        update.getDriverId(), error.getMessage()))
                .subscribe();
    }

    private double randomBetween(double min, double max) {
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }
}
