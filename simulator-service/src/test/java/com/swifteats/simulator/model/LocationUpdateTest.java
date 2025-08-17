package com.swifteats.simulator.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocationUpdateTest {

    @Test
    void testBasicConstructor() {
        // Arrange
        Long driverId = 123L;
        Double latitude = 37.75;
        Double longitude = -122.45;

        // Act
        LocationUpdate update = new LocationUpdate(driverId, latitude, longitude);

        // Assert
        assertEquals(driverId, update.getDriverId());
        assertEquals(latitude, update.getLatitude());
        assertEquals(longitude, update.getLongitude());
        assertNull(update.getHeading());
        assertNull(update.getSpeed());
        assertNull(update.getAccuracy());
    }

    @Test
    void testFullConstructor() {
        // Arrange
        Long driverId = 123L;
        Double latitude = 37.75;
        Double longitude = -122.45;
        Double heading = 45.0;
        Double speed = 30.0;
        Double accuracy = 5.0;

        // Act
        LocationUpdate update = new LocationUpdate(driverId, latitude, longitude);
        update.setHeading(heading);
        update.setSpeed(speed);
        update.setAccuracy(accuracy);

        // Assert
        assertEquals(driverId, update.getDriverId());
        assertEquals(latitude, update.getLatitude());
        assertEquals(longitude, update.getLongitude());
        assertEquals(heading, update.getHeading());
        assertEquals(speed, update.getSpeed());
        assertEquals(accuracy, update.getAccuracy());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        // Arrange
        Long driverId = 123L;
        Double latitude = 37.75;
        Double longitude = -122.45;
        Double heading = 45.0;
        Double speed = 30.0;
        Double accuracy = 5.0;

        // Act
        LocationUpdate update = new LocationUpdate();
        update.setDriverId(driverId);
        update.setLatitude(latitude);
        update.setLongitude(longitude);
        update.setHeading(heading);
        update.setSpeed(speed);
        update.setAccuracy(accuracy);

        // Assert
        assertEquals(driverId, update.getDriverId());
        assertEquals(latitude, update.getLatitude());
        assertEquals(longitude, update.getLongitude());
        assertEquals(heading, update.getHeading());
        assertEquals(speed, update.getSpeed());
        assertEquals(accuracy, update.getAccuracy());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        LocationUpdate update1 = new LocationUpdate(123L, 37.75, -122.45);
        LocationUpdate update2 = new LocationUpdate(123L, 37.75, -122.45);
        LocationUpdate update3 = new LocationUpdate(456L, 37.75, -122.45);

        // Assert - Equality
        assertEquals(update1, update2);
        assertEquals(update1.hashCode(), update2.hashCode());

        // Assert - Inequality
        assertNotEquals(update1, update3);
        assertNotEquals(update1.hashCode(), update3.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        LocationUpdate update = new LocationUpdate(123L, 37.75, -122.45);
        update.setHeading(45.0);
        update.setSpeed(30.0);
        update.setAccuracy(5.0);

        // Act
        String result = update.toString();

        // Assert
        assertTrue(result.contains("driverId=123"));
        assertTrue(result.contains("latitude=37.75"));
        assertTrue(result.contains("longitude=-122.45"));
        assertTrue(result.contains("heading=45.0"));
        assertTrue(result.contains("speed=30.0"));
        assertTrue(result.contains("accuracy=5.0"));
    }
}
