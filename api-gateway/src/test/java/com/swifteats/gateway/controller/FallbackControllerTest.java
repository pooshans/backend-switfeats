package com.swifteats.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FallbackControllerTest {

    private final FallbackController fallbackController = new FallbackController();

    @Test
    void orderServiceFallback_shouldReturnServiceUnavailable() {
        // Act
        ResponseEntity<Map<String, String>> response = fallbackController.orderServiceFallback();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("error", responseBody.get("status"));
        assertEquals("Order Service is currently unavailable. Please try again later.", responseBody.get("message"));
    }

    @Test
    void restaurantServiceFallback_shouldReturnServiceUnavailable() {
        // Act
        ResponseEntity<Map<String, String>> response = fallbackController.restaurantServiceFallback();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("error", responseBody.get("status"));
        assertEquals("Restaurant Service is currently unavailable. Please try again later.",
                responseBody.get("message"));
    }

    @Test
    void driverServiceFallback_shouldReturnServiceUnavailable() {
        // Act
        ResponseEntity<Map<String, String>> response = fallbackController.driverServiceFallback();

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("error", responseBody.get("status"));
        assertEquals("Driver Service is currently unavailable. Please try again later.", responseBody.get("message"));
    }
}
