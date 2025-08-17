package com.swifteats.driver.service;

import com.swifteats.driver.dto.DriverDTO;
import com.swifteats.driver.dto.DriverStatusUpdateDTO;
import com.swifteats.driver.dto.LocationDTO;
import com.swifteats.driver.dto.LocationUpdateDTO;
import com.swifteats.driver.model.Driver;
import com.swifteats.driver.model.DriverLocation;
import com.swifteats.driver.model.DriverStatus;
import com.swifteats.driver.repository.DriverLocationRepository;
import com.swifteats.driver.repository.DriverRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverLocationRepository locationRepository;
    private final RedisTemplate<String, LocationDTO> locationRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String DRIVER_LOCATION_KEY_PREFIX = "driver:location:";
    private static final long LOCATION_CACHE_EXPIRATION = 30; // 30 seconds

    @Transactional
    public DriverDTO createDriver(DriverDTO driverDTO) {
        Driver driver = convertToEntity(driverDTO);
        driver = driverRepository.save(driver);
        return convertToDTO(driver);
    }

    @Transactional(readOnly = true)
    public List<DriverDTO> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DriverDTO> getDriverById(Long id) {
        return driverRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    @CircuitBreaker(name = "driverService", fallbackMethod = "updateLocationFallback")
    public LocationDTO updateDriverLocation(LocationUpdateDTO locationUpdate) {
        Driver driver = driverRepository.findById(locationUpdate.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + locationUpdate.getDriverId()));

        DriverLocation location = DriverLocation.builder()
                .driver(driver)
                .latitude(locationUpdate.getLatitude())
                .longitude(locationUpdate.getLongitude())
                .heading(locationUpdate.getHeading())
                .speed(locationUpdate.getSpeed())
                .accuracy(locationUpdate.getAccuracy())
                .timestamp(LocalDateTime.now())
                .build();

        location = locationRepository.save(location);
        driver.addLocationUpdate(location);

        LocationDTO locationDTO = convertToLocationDTO(location);

        // Cache the location in Redis for fast retrieval
        String redisKey = DRIVER_LOCATION_KEY_PREFIX + driver.getId();
        locationRedisTemplate.opsForValue().set(redisKey, locationDTO, LOCATION_CACHE_EXPIRATION, TimeUnit.SECONDS);

        // Broadcast location update through WebSocket
        messagingTemplate.convertAndSend("/topic/driver/" + driver.getId() + "/location", locationDTO);

        return locationDTO;
    }

    public LocationDTO updateLocationFallback(LocationUpdateDTO locationUpdate, Throwable t) {
        log.error("Circuit breaker triggered for driver location update: {}", t.getMessage());
        return LocationDTO.builder()
                .latitude(locationUpdate.getLatitude())
                .longitude(locationUpdate.getLongitude())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> getDriverLocationHistory(Long driverId, LocalDateTime startTime) {
        return locationRepository.findDriverLocationHistory(driverId, startTime)
                .stream()
                .map(this::convertToLocationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DriverDTO updateDriverStatus(DriverStatusUpdateDTO statusUpdate) {
        Driver driver = driverRepository.findById(statusUpdate.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + statusUpdate.getDriverId()));

        driver.setStatus(statusUpdate.getStatus());
        driver = driverRepository.save(driver);

        // Broadcast status update through WebSocket
        DriverDTO driverDTO = convertToDTO(driver);
        messagingTemplate.convertAndSend("/topic/driver/" + driver.getId() + "/status", driverDTO);

        return driverDTO;
    }

    @Transactional(readOnly = true)
    public List<DriverDTO> getAvailableDriversNearby(Double latitude, Double longitude, Double radiusInMeters) {
        // Get all available drivers
        List<Driver> availableDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE);

        // Filter drivers based on their location
        return availableDrivers.stream()
                .filter(driver -> isDriverNearby(driver, latitude, longitude, radiusInMeters))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean isDriverNearby(Driver driver, Double latitude, Double longitude, Double radiusInMeters) {
        // Get the most recent location from the driver's location history
        if (driver.getLocationHistory() == null || driver.getLocationHistory().isEmpty()) {
            return false;
        }

        // Sort location history by timestamp to get the most recent location
        DriverLocation latestLocation = driver.getLocationHistory().stream()
                .sorted(Comparator.comparing(DriverLocation::getTimestamp).reversed())
                .findFirst()
                .orElse(null);

        if (latestLocation == null) {
            return false;
        }

        // Calculate distance using the Haversine formula
        return calculateDistance(
                latitude, longitude,
                latestLocation.getLatitude(), latestLocation.getLongitude()) <= radiusInMeters;
    }

    /**
     * Calculate the distance between two points using the Haversine formula.
     * 
     * @return Distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Transactional(readOnly = true)
    public LocationDTO getDriverCurrentLocation(Long driverId) {
        // Try to get from cache first
        String redisKey = DRIVER_LOCATION_KEY_PREFIX + driverId;
        LocationDTO cachedLocation = locationRedisTemplate.opsForValue().get(redisKey);

        if (cachedLocation != null) {
            return cachedLocation;
        }

        // If not in cache, get from database
        DriverLocation location = locationRepository.findLatestLocationByDriverId(driverId);
        if (location != null) {
            LocationDTO locationDTO = convertToLocationDTO(location);
            // Cache the result
            locationRedisTemplate.opsForValue().set(redisKey, locationDTO, LOCATION_CACHE_EXPIRATION, TimeUnit.SECONDS);
            return locationDTO;
        }

        return null;
    }

    private Driver convertToEntity(DriverDTO driverDTO) {
        return Driver.builder()
                .name(driverDTO.getName())
                .email(driverDTO.getEmail())
                .phone(driverDTO.getPhone())
                .vehicleType(driverDTO.getVehicleType())
                .vehiclePlate(driverDTO.getVehiclePlate())
                .status(driverDTO.getStatus() != null ? driverDTO.getStatus() : DriverStatus.OFFLINE)
                .build();
    }

    private DriverDTO convertToDTO(Driver driver) {
        LocationDTO currentLocation = null;
        if (driver.getLocationHistory() != null && !driver.getLocationHistory().isEmpty()) {
            DriverLocation latestLocation = driver.getLocationHistory()
                    .get(driver.getLocationHistory().size() - 1);
            currentLocation = convertToLocationDTO(latestLocation);
        }

        return DriverDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .vehicleType(driver.getVehicleType())
                .vehiclePlate(driver.getVehiclePlate())
                .status(driver.getStatus())
                .currentLocation(currentLocation)
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }

    private LocationDTO convertToLocationDTO(DriverLocation location) {
        return LocationDTO.builder()
                .id(location.getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .heading(location.getHeading())
                .speed(location.getSpeed())
                .accuracy(location.getAccuracy())
                .timestamp(location.getTimestamp())
                .build();
    }
}
