package com.swifteats.simulator.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulatedDriverTest {

    @Test
    void testBuilder() {
        // Arrange & Act
        SimulatedDriver driver = SimulatedDriver.builder()
                .id(123L)
                .latitude(37.75)
                .longitude(-122.45)
                .heading(45.0)
                .speed(30.0)
                .accuracy(5.0)
                .destLatitude(37.76)
                .destLongitude(-122.46)
                .stepsRemaining(100.0)
                .latStep(0.001)
                .lngStep(0.001)
                .build();

        // Assert
        assertEquals(123L, driver.getId());
        assertEquals(37.75, driver.getLatitude());
        assertEquals(-122.45, driver.getLongitude());
        assertEquals(45.0, driver.getHeading());
        assertEquals(30.0, driver.getSpeed());
        assertEquals(5.0, driver.getAccuracy());
        assertEquals(37.76, driver.getDestLatitude());
        assertEquals(-122.46, driver.getDestLongitude());
        assertEquals(100.0, driver.getStepsRemaining());
        assertEquals(0.001, driver.getLatStep());
        assertEquals(0.001, driver.getLngStep());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        // Arrange
        SimulatedDriver driver = new SimulatedDriver();

        // Act
        driver.setId(123L);
        driver.setLatitude(37.75);
        driver.setLongitude(-122.45);
        driver.setHeading(45.0);
        driver.setSpeed(30.0);
        driver.setAccuracy(5.0);
        driver.setDestLatitude(37.76);
        driver.setDestLongitude(-122.46);
        driver.setStepsRemaining(100.0);
        driver.setLatStep(0.001);
        driver.setLngStep(0.001);

        // Assert
        assertEquals(123L, driver.getId());
        assertEquals(37.75, driver.getLatitude());
        assertEquals(-122.45, driver.getLongitude());
        assertEquals(45.0, driver.getHeading());
        assertEquals(30.0, driver.getSpeed());
        assertEquals(5.0, driver.getAccuracy());
        assertEquals(37.76, driver.getDestLatitude());
        assertEquals(-122.46, driver.getDestLongitude());
        assertEquals(100.0, driver.getStepsRemaining());
        assertEquals(0.001, driver.getLatStep());
        assertEquals(0.001, driver.getLngStep());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange & Act
        SimulatedDriver driver = new SimulatedDriver(
                123L, 37.75, -122.45, 45.0, 30.0, 5.0,
                37.76, -122.46, 100.0, 0.001, 0.001);

        // Assert
        assertEquals(123L, driver.getId());
        assertEquals(37.75, driver.getLatitude());
        assertEquals(-122.45, driver.getLongitude());
        assertEquals(45.0, driver.getHeading());
        assertEquals(30.0, driver.getSpeed());
        assertEquals(5.0, driver.getAccuracy());
        assertEquals(37.76, driver.getDestLatitude());
        assertEquals(-122.46, driver.getDestLongitude());
        assertEquals(100.0, driver.getStepsRemaining());
        assertEquals(0.001, driver.getLatStep());
        assertEquals(0.001, driver.getLngStep());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        SimulatedDriver driver1 = SimulatedDriver.builder()
                .id(123L)
                .latitude(37.75)
                .longitude(-122.45)
                .build();

        SimulatedDriver driver2 = SimulatedDriver.builder()
                .id(123L)
                .latitude(37.75)
                .longitude(-122.45)
                .build();

        SimulatedDriver driver3 = SimulatedDriver.builder()
                .id(456L)
                .latitude(37.75)
                .longitude(-122.45)
                .build();

        // Assert - Equality
        assertEquals(driver1, driver2);
        assertEquals(driver1.hashCode(), driver2.hashCode());

        // Assert - Inequality
        assertNotEquals(driver1, driver3);
        assertNotEquals(driver1.hashCode(), driver3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        SimulatedDriver driver = SimulatedDriver.builder()
                .id(123L)
                .latitude(37.75)
                .longitude(-122.45)
                .heading(45.0)
                .speed(30.0)
                .accuracy(5.0)
                .destLatitude(37.76)
                .destLongitude(-122.46)
                .stepsRemaining(100.0)
                .latStep(0.001)
                .lngStep(0.001)
                .build();

        // Act
        String result = driver.toString();

        // Assert
        assertTrue(result.contains("id=123"));
        assertTrue(result.contains("latitude=37.75"));
        assertTrue(result.contains("longitude=-122.45"));
        assertTrue(result.contains("heading=45.0"));
        assertTrue(result.contains("speed=30.0"));
        assertTrue(result.contains("accuracy=5.0"));
        assertTrue(result.contains("destLatitude=37.76"));
        assertTrue(result.contains("destLongitude=-122.46"));
        assertTrue(result.contains("stepsRemaining=100.0"));
        assertTrue(result.contains("latStep=0.001"));
        assertTrue(result.contains("lngStep=0.001"));
    }
}
