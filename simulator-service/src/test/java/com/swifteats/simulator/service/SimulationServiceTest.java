package com.swifteats.simulator.service;

import com.swifteats.simulator.model.LocationUpdate;
import com.swifteats.simulator.model.SimulatedDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SimulationService simulationService;

    @BeforeEach
    void setUp() {
        // Set up test values for the service properties
        ReflectionTestUtils.setField(simulationService, "driverCount", 10);
        ReflectionTestUtils.setField(simulationService, "apiUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(simulationService, "updateIntervalMs", 500L);

        // Use lenient mode for mocks that may not be used in all tests
        // Mock WebClient chain
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);

        // Use Mockito.doReturn for difficult generics
        lenient().doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));
    }

    @Test
    void initializeDrivers_shouldCreateDriversWithCorrectParameters() throws Exception {
        // Arrange
        int expectedDriverCount = 10;

        // Act
        simulationService.initializeDrivers();

        // Assert - Get the private driversMap field via reflection
        Field driversMapField = SimulationService.class.getDeclaredField("driversMap");
        driversMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, SimulatedDriver> driversMap = (Map<Long, SimulatedDriver>) driversMapField.get(simulationService);

        // Verify that the correct number of drivers were created
        assertEquals(expectedDriverCount, driversMap.size());

        // Verify that each driver has required properties
        driversMap.values().forEach(driver -> {
            assertNotNull(driver.getId());
            assertNotNull(driver.getLatitude());
            assertNotNull(driver.getLongitude());
            assertNotNull(driver.getDestLatitude());
            assertNotNull(driver.getDestLongitude());
            assertNotNull(driver.getHeading());
            assertNotNull(driver.getSpeed());
            assertNotNull(driver.getAccuracy());
            assertNotNull(driver.getStepsRemaining());
            assertNotNull(driver.getLatStep());
            assertNotNull(driver.getLngStep());

            // Verify that values are within expected bounds
            assertTrue(driver.getLatitude() >= 37.7 && driver.getLatitude() <= 37.8);
            assertTrue(driver.getLongitude() >= -122.5 && driver.getLongitude() <= -122.4);
            assertTrue(driver.getDestLatitude() >= 37.7 && driver.getDestLatitude() <= 37.8);
            assertTrue(driver.getDestLongitude() >= -122.5 && driver.getDestLongitude() <= -122.4);
            assertTrue(driver.getHeading() >= 0 && driver.getHeading() < 360);
            assertTrue(driver.getSpeed() >= 15 && driver.getSpeed() <= 60);
            assertTrue(driver.getAccuracy() >= 3 && driver.getAccuracy() <= 10);
            assertTrue(driver.getStepsRemaining() >= 50 && driver.getStepsRemaining() <= 150);
        });
    }

    @Test
    void updateDriverLocations_shouldUpdateDriverPositions() throws Exception {
        // Arrange
        // First initialize some drivers
        simulationService.initializeDrivers();

        // Get the drivers map via reflection
        Field driversMapField = SimulationService.class.getDeclaredField("driversMap");
        driversMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, SimulatedDriver> driversMap = (Map<Long, SimulatedDriver>) driversMapField.get(simulationService);

        // Make a copy of the initial positions
        Map<Long, Double[]> initialPositions = new ConcurrentHashMap<>();
        driversMap.forEach((id, driver) -> {
            initialPositions.put(id, new Double[] { driver.getLatitude(), driver.getLongitude() });
        });

        // Act
        simulationService.updateDriverLocations();

        // Assert
        // Check that positions have been updated
        driversMap.forEach((id, driver) -> {
            Double[] initialPos = initialPositions.get(id);
            assertNotEquals(initialPos[0], driver.getLatitude());
            assertNotEquals(initialPos[1], driver.getLongitude());
        });

        // Verify WebClient was called for sending batch updates
        verify(webClientBuilder, times(1)).baseUrl("http://localhost:8080");
        verify(webClient, times(1)).post();
        verify(requestBodyUriSpec, times(1)).uri("/api/v1/drivers/location/batch");
    }

    @Test
    void updateDriverLocations_shouldCreateNewDestinationWhenReached() throws Exception {
        // Arrange
        // Create a drivers map with a single driver who's about to reach destination (0
        // steps)
        Map<Long, SimulatedDriver> driversMap = new ConcurrentHashMap<>();
        SimulatedDriver driver = SimulatedDriver.builder()
                .id(1L)
                .latitude(37.75)
                .longitude(-122.45)
                .destLatitude(37.76)
                .destLongitude(-122.46)
                .heading(45.0)
                .speed(30.0)
                .accuracy(5.0)
                .stepsRemaining(0.0) // No steps remaining, so should create new destination
                .latStep(0.001)
                .lngStep(0.001)
                .build();

        driversMap.put(1L, driver);

        // Set the driversMap via reflection
        Field driversMapField = SimulationService.class.getDeclaredField("driversMap");
        driversMapField.setAccessible(true);
        driversMapField.set(simulationService, driversMap);

        // Remember original destination
        Double origDestLat = driver.getDestLatitude();
        Double origDestLong = driver.getDestLongitude();

        // Act
        simulationService.updateDriverLocations();

        // Assert
        // Verify new destination was created
        assertNotEquals(origDestLat, driver.getDestLatitude());
        assertNotEquals(origDestLong, driver.getDestLongitude());
        assertTrue(driver.getStepsRemaining() > 0);
    }

    @Test
    void sendBatchUpdates_shouldHandleErrors() throws Exception {
        // Arrange
        // Setup WebClient to simulate an error
        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.error(new RuntimeException("400 Bad Request")))
                .thenReturn(Mono.just(ResponseEntity.ok().build()))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));

        // Create test data
        simulationService.initializeDrivers();

        // Get private method via reflection
        java.lang.reflect.Method sendBatchUpdatesMethod = SimulationService.class.getDeclaredMethod(
                "sendBatchUpdates", List.class);
        sendBatchUpdatesMethod.setAccessible(true);

        // Create a test batch
        List<LocationUpdate> updates = List.of(
                new LocationUpdate(1L, 37.75, -122.45),
                new LocationUpdate(2L, 37.76, -122.46));

        // Act
        sendBatchUpdatesMethod.invoke(simulationService, updates);

        // Assert
        // Verify that it attempted to send individual updates after batch failure
        // We need to wait a bit for reactive operations to complete
        Thread.sleep(500);

        verify(requestBodyUriSpec, times(1)).uri("/api/v1/drivers/location/batch");
        verify(requestBodyUriSpec, times(2)).uri("/api/v1/drivers/location");
    }

    @Test
    void randomBetween_shouldReturnValueInRange() throws Exception {
        // Get private method via reflection
        java.lang.reflect.Method randomBetweenMethod = SimulationService.class.getDeclaredMethod(
                "randomBetween", double.class, double.class);
        randomBetweenMethod.setAccessible(true);

        // Test 100 times to ensure we're getting values in the expected range
        for (int i = 0; i < 100; i++) {
            double result = (double) randomBetweenMethod.invoke(simulationService, 10.0, 20.0);
            assertTrue(result >= 10.0 && result <= 20.0);
        }
    }
}
