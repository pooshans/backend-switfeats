package com.swifteats.driver.controller;

import com.swifteats.driver.dto.DriverDTO;
import com.swifteats.driver.dto.DriverStatusUpdateDTO;
import com.swifteats.driver.dto.LocationDTO;
import com.swifteats.driver.dto.LocationUpdateDTO;
import com.swifteats.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<DriverDTO> createDriver(@Valid @RequestBody DriverDTO driverDTO) {
        log.info("Creating new driver: {}", driverDTO.getEmail());
        DriverDTO createdDriver = driverService.createDriver(driverDTO);
        return new ResponseEntity<>(createdDriver, HttpStatus.CREATED);
    }

    @PostMapping("/location/batch")
    public ResponseEntity<Void> updateDriverLocationBatch(@Valid @RequestBody List<LocationUpdateDTO> locationUpdates) {
        log.info("Received batch location update for {} drivers", locationUpdates.size());
        locationUpdates.forEach(driverService::updateDriverLocation);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DriverDTO>> getAllDrivers() {
        log.info("Fetching all drivers");
        List<DriverDTO> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable Long id) {
        log.info("Fetching driver with ID: {}", id);
        return driverService.getDriverById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/location")
    public ResponseEntity<LocationDTO> updateDriverLocation(@Valid @RequestBody LocationUpdateDTO locationUpdate) {
        log.info("Updating location for driver ID: {}", locationUpdate.getDriverId());
        LocationDTO updatedLocation = driverService.updateDriverLocation(locationUpdate);
        return ResponseEntity.ok(updatedLocation);
    }

    @GetMapping("/{id}/location/history")
    public ResponseEntity<List<LocationDTO>> getDriverLocationHistory(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDateTime since) {

        LocalDateTime startTime = since != null ? since : LocalDateTime.now().minusHours(1);
        log.info("Fetching location history for driver ID: {} since {}", id, startTime);

        List<LocationDTO> locationHistory = driverService.getDriverLocationHistory(id, startTime);
        return ResponseEntity.ok(locationHistory);
    }

    @GetMapping("/{id}/location/current")
    public ResponseEntity<LocationDTO> getDriverCurrentLocation(@PathVariable Long id) {
        log.info("Fetching current location for driver ID: {}", id);
        LocationDTO currentLocation = driverService.getDriverCurrentLocation(id);

        if (currentLocation != null) {
            return ResponseEntity.ok(currentLocation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/status")
    public ResponseEntity<DriverDTO> updateDriverStatus(@Valid @RequestBody DriverStatusUpdateDTO statusUpdate) {
        log.info("Updating status for driver ID: {} to {}", statusUpdate.getDriverId(), statusUpdate.getStatus());
        DriverDTO updatedDriver = driverService.updateDriverStatus(statusUpdate);
        return ResponseEntity.ok(updatedDriver);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DriverDTO>> getNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5000") Double radius) {

        log.info("Finding available drivers near lat: {}, lng: {} within {}m", latitude, longitude, radius);
        List<DriverDTO> nearbyDrivers = driverService.getAvailableDriversNearby(latitude, longitude, radius);
        return ResponseEntity.ok(nearbyDrivers);
    }
}
