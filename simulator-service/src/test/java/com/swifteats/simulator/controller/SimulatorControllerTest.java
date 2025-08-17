package com.swifteats.simulator.controller;

import com.swifteats.simulator.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulatorControllerTest {

    @Mock
    private SimulationService simulationService;

    @InjectMocks
    private SimulatorController simulatorController;

    @BeforeEach
    void setUp() {
        // Set up test values for the controller properties
        ReflectionTestUtils.setField(simulatorController, "driverCount", 100);
        ReflectionTestUtils.setField(simulatorController, "updateIntervalMs", 500L);
    }

    @Test
    void getSimulationStatus_shouldReturnCorrectValues() {
        // Act
        ResponseEntity<Map<String, Object>> response = simulatorController.getSimulationStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("active"));
        assertEquals(100, body.get("driverCount"));
        assertEquals(500L, body.get("updateIntervalMs"));
        assertEquals(200.0, body.get("updatesPerSecond"));
    }

    @Test
    void restartSimulation_shouldCallServiceAndReturnSuccess() {
        // Act
        ResponseEntity<Map<String, String>> response = simulatorController.restartSimulation();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertTrue(body.get("message").contains("restarted with 100 drivers"));

        // Verify service interaction
        verify(simulationService, times(1)).initializeDrivers();
    }

    @Test
    void setDriverCount_withValidCount_shouldUpdateAndReturnSuccess() {
        // Arrange
        int newDriverCount = 50;

        // Act
        ResponseEntity<Map<String, String>> response = simulatorController.setDriverCount(newDriverCount);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("Driver count updated to 50", body.get("message"));

        // Verify service interaction and state update
        verify(simulationService, times(1)).initializeDrivers();

        // Verify field updated (using reflection to check private field)
        Object fieldValue = ReflectionTestUtils.getField(simulatorController, "driverCount");
        assertNotNull(fieldValue);
        assertEquals(newDriverCount, (int) fieldValue);
    }

    @Test
    void setDriverCount_withTooLowCount_shouldReturnBadRequest() {
        // Arrange
        int invalidDriverCount = 0;

        // Act
        ResponseEntity<Map<String, String>> response = simulatorController.setDriverCount(invalidDriverCount);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").contains("must be between"));

        // Verify no service interaction
        verify(simulationService, never()).initializeDrivers();
    }

    @Test
    void setDriverCount_withTooHighCount_shouldReturnBadRequest() {
        // Arrange
        int invalidDriverCount = 60000;

        // Act
        ResponseEntity<Map<String, String>> response = simulatorController.setDriverCount(invalidDriverCount);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertTrue(body.get("message").contains("must be between"));

        // Verify no service interaction
        verify(simulationService, never()).initializeDrivers();
    }
}
