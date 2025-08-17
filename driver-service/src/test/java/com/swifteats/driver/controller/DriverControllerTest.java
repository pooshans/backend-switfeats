package com.swifteats.driver.controller;

import com.swifteats.driver.dto.DriverDTO;
import com.swifteats.driver.dto.DriverStatusUpdateDTO;
import com.swifteats.driver.dto.LocationDTO;
import com.swifteats.driver.dto.LocationUpdateDTO;
import com.swifteats.driver.model.DriverStatus;
import com.swifteats.driver.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    private DriverDTO testDriver;
    private LocationUpdateDTO testLocationUpdate;
    private LocationDTO testLocation;
    private List<LocationUpdateDTO> batchLocationUpdates;
    private DriverStatusUpdateDTO testStatusUpdate;

    @BeforeEach
    void setUp() {
        // Setup test driver
        testDriver = DriverDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .vehicleType("SEDAN")
                .vehiclePlate("ABC123")
                .status(DriverStatus.AVAILABLE)
                .build();

        // Setup test location
        testLocation = LocationDTO.builder()
                .id(1L)
                .latitude(37.7749)
                .longitude(-122.4194)
                .heading(90.0)
                .speed(30.0)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();

        // Setup test location update
        testLocationUpdate = new LocationUpdateDTO();
        testLocationUpdate.setDriverId(1L);
        testLocationUpdate.setLatitude(37.7749);
        testLocationUpdate.setLongitude(-122.4194);
        testLocationUpdate.setHeading(90.0);
        testLocationUpdate.setSpeed(30.0);
        testLocationUpdate.setAccuracy(5.0);

        // Setup batch location updates
        batchLocationUpdates = new ArrayList<>();
        batchLocationUpdates.add(testLocationUpdate);

        // Setup test status update
        testStatusUpdate = new DriverStatusUpdateDTO();
        testStatusUpdate.setDriverId(1L);
        testStatusUpdate.setStatus(DriverStatus.BUSY);
    }

    @Test
    void createDriver_shouldReturnCreatedDriver() {
        // Arrange
        when(driverService.createDriver(any(DriverDTO.class))).thenReturn(testDriver);

        // Act
        ResponseEntity<DriverDTO> response = driverController.createDriver(testDriver);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDriver, response.getBody());
        verify(driverService, times(1)).createDriver(testDriver);
    }

    @Test
    void updateDriverLocationBatch_shouldUpdateLocations() {
        // Arrange
        // No need to mock anything as this is a void method

        // Act
        ResponseEntity<Void> response = driverController.updateDriverLocationBatch(batchLocationUpdates);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(driverService, times(1)).updateDriverLocation(any(LocationUpdateDTO.class));
    }

    @Test
    void getAllDrivers_shouldReturnAllDrivers() {
        // Arrange
        List<DriverDTO> drivers = List.of(testDriver);
        when(driverService.getAllDrivers()).thenReturn(drivers);

        // Act
        ResponseEntity<List<DriverDTO>> response = driverController.getAllDrivers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<DriverDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(testDriver, responseBody.get(0));
        verify(driverService, times(1)).getAllDrivers();
    }

    @Test
    void getDriverById_whenDriverExists_shouldReturnDriver() {
        // Arrange
        when(driverService.getDriverById(1L)).thenReturn(Optional.of(testDriver));

        // Act
        ResponseEntity<DriverDTO> response = driverController.getDriverById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDriver, response.getBody());
        verify(driverService, times(1)).getDriverById(1L);
    }

    @Test
    void getDriverById_whenDriverDoesNotExist_shouldReturnNotFound() {
        // Arrange
        when(driverService.getDriverById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<DriverDTO> response = driverController.getDriverById(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(driverService, times(1)).getDriverById(99L);
    }

    @Test
    void updateDriverLocation_shouldReturnUpdatedLocation() {
        // Arrange
        when(driverService.updateDriverLocation(any(LocationUpdateDTO.class))).thenReturn(testLocation);

        // Act
        ResponseEntity<LocationDTO> response = driverController.updateDriverLocation(testLocationUpdate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testLocation, response.getBody());
        verify(driverService, times(1)).updateDriverLocation(testLocationUpdate);
    }

    @Test
    void getDriverLocationHistory_shouldReturnLocationHistory() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<LocationDTO> locationHistory = List.of(testLocation);
        when(driverService.getDriverLocationHistory(eq(1L), any(LocalDateTime.class))).thenReturn(locationHistory);

        // Act
        ResponseEntity<List<LocationDTO>> response = driverController.getDriverLocationHistory(1L, since);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<LocationDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(testLocation, responseBody.get(0));
        verify(driverService, times(1)).getDriverLocationHistory(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getDriverCurrentLocation_whenLocationExists_shouldReturnLocation() {
        // Arrange
        when(driverService.getDriverCurrentLocation(1L)).thenReturn(testLocation);

        // Act
        ResponseEntity<LocationDTO> response = driverController.getDriverCurrentLocation(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testLocation, response.getBody());
        verify(driverService, times(1)).getDriverCurrentLocation(1L);
    }

    @Test
    void getDriverCurrentLocation_whenLocationDoesNotExist_shouldReturnNotFound() {
        // Arrange
        when(driverService.getDriverCurrentLocation(99L)).thenReturn(null);

        // Act
        ResponseEntity<LocationDTO> response = driverController.getDriverCurrentLocation(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(driverService, times(1)).getDriverCurrentLocation(99L);
    }

    @Test
    void updateDriverStatus_shouldReturnUpdatedDriver() {
        // Arrange
        when(driverService.updateDriverStatus(any(DriverStatusUpdateDTO.class))).thenReturn(testDriver);

        // Act
        ResponseEntity<DriverDTO> response = driverController.updateDriverStatus(testStatusUpdate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDriver, response.getBody());
        verify(driverService, times(1)).updateDriverStatus(testStatusUpdate);
    }

    @Test
    void getNearbyDrivers_shouldReturnNearbyDrivers() {
        // Arrange
        double lat = 37.7749;
        double lng = -122.4194;
        double radius = 5000.0;
        List<DriverDTO> nearbyDrivers = List.of(testDriver);
        when(driverService.getAvailableDriversNearby(lat, lng, radius)).thenReturn(nearbyDrivers);

        // Act
        ResponseEntity<List<DriverDTO>> response = driverController.getNearbyDrivers(lat, lng, radius);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<DriverDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(testDriver, responseBody.get(0));
        verify(driverService, times(1)).getAvailableDriversNearby(lat, lng, radius);
    }
}
