package com.swifteats.restaurant.controller;

import com.swifteats.restaurant.dto.RestaurantDTO;
import com.swifteats.restaurant.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private RestaurantController restaurantController;

    private UUID testId;
    private RestaurantDTO testRestaurant;
    private List<RestaurantDTO> testRestaurantList;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        // Create a test restaurant
        testRestaurant = RestaurantDTO.builder()
                .id(testId)
                .name("Test Restaurant")
                .description("Test Description")
                .address("123 Test St, Test City")
                .cuisine("Italian")
                .phoneNumber("123-456-7890")
                .rating(4.5f)
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(22, 0))
                .deliveryFee(5.0)
                .estimatedDeliveryTime(30)
                .logoUrl("http://example.com/logo.png")
                .isActive(true)
                .isOpen(true)
                .build();

        // Create a list of test restaurants
        testRestaurantList = Arrays.asList(testRestaurant);
    }

    @Test
    void getAllRestaurants_shouldReturnAllRestaurants() {
        // Arrange
        when(restaurantService.getAllRestaurants()).thenReturn(testRestaurantList);

        // Act
        ResponseEntity<List<RestaurantDTO>> response = restaurantController.getAllRestaurants();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RestaurantDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(testRestaurant.getName(), responseBody.get(0).getName());
        verify(restaurantService, times(1)).getAllRestaurants();
    }

    @Test
    void getRestaurantById_whenRestaurantExists_shouldReturnRestaurant() {
        // Arrange
        when(restaurantService.getRestaurantById(testId)).thenReturn(Optional.of(testRestaurant));

        // Act
        ResponseEntity<RestaurantDTO> response = restaurantController.getRestaurantById(testId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RestaurantDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testRestaurant.getId(), responseBody.getId());
        assertEquals(testRestaurant.getName(), responseBody.getName());
        verify(restaurantService, times(1)).getRestaurantById(testId);
    }

    @Test
    void getRestaurantById_whenRestaurantDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(restaurantService.getRestaurantById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<RestaurantDTO> response = restaurantController.getRestaurantById(nonExistentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(restaurantService, times(1)).getRestaurantById(nonExistentId);
    }

    @Test
    void getRestaurantsByCuisine_shouldReturnMatchingRestaurants() {
        // Arrange
        String cuisine = "Italian";
        when(restaurantService.getRestaurantsByCuisine(cuisine)).thenReturn(testRestaurantList);

        // Act
        ResponseEntity<List<RestaurantDTO>> response = restaurantController.getRestaurantsByCuisine(cuisine);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RestaurantDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals("Italian", responseBody.get(0).getCuisine());
        verify(restaurantService, times(1)).getRestaurantsByCuisine(cuisine);
    }

    @Test
    void getOpenRestaurants_shouldReturnOpenRestaurants() {
        // Arrange
        when(restaurantService.getOpenRestaurants()).thenReturn(testRestaurantList);

        // Act
        ResponseEntity<List<RestaurantDTO>> response = restaurantController.getOpenRestaurants();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<RestaurantDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get(0).isOpen());
        verify(restaurantService, times(1)).getOpenRestaurants();
    }

    @Test
    void createRestaurant_shouldCreateAndReturnRestaurant() {
        // Arrange
        when(restaurantService.createRestaurant(any(RestaurantDTO.class))).thenReturn(testRestaurant);

        // Create a restaurant without ID (simulating creation request)
        RestaurantDTO newRestaurant = RestaurantDTO.builder()
                .name("New Restaurant")
                .cuisine("Italian")
                .build();

        // Act
        ResponseEntity<RestaurantDTO> response = restaurantController.createRestaurant(newRestaurant);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        RestaurantDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testRestaurant.getId(), responseBody.getId());
        assertEquals(testRestaurant.getName(), responseBody.getName());
        verify(restaurantService, times(1)).createRestaurant(any(RestaurantDTO.class));
    }

    @Test
    void updateRestaurant_whenRestaurantExists_shouldUpdateAndReturnRestaurant() {
        // Arrange
        when(restaurantService.updateRestaurant(eq(testId), any(RestaurantDTO.class)))
                .thenReturn(Optional.of(testRestaurant));

        RestaurantDTO updateRequest = RestaurantDTO.builder()
                .name("Updated Restaurant")
                .description("Updated Description")
                .build();

        // Act
        ResponseEntity<RestaurantDTO> response = restaurantController.updateRestaurant(testId, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RestaurantDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testRestaurant.getId(), responseBody.getId());
        assertEquals(testRestaurant.getName(), responseBody.getName());
        verify(restaurantService, times(1)).updateRestaurant(eq(testId), any(RestaurantDTO.class));
    }

    @Test
    void updateRestaurant_whenRestaurantDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(restaurantService.updateRestaurant(eq(nonExistentId), any(RestaurantDTO.class)))
                .thenReturn(Optional.empty());

        RestaurantDTO updateRequest = RestaurantDTO.builder()
                .name("Updated Restaurant")
                .description("Updated Description")
                .build();

        // Act
        ResponseEntity<RestaurantDTO> response = restaurantController.updateRestaurant(nonExistentId, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(restaurantService, times(1)).updateRestaurant(eq(nonExistentId), any(RestaurantDTO.class));
    }

    @Test
    void deleteRestaurant_whenRestaurantExists_shouldReturnNoContent() {
        // Arrange
        when(restaurantService.deleteRestaurant(testId)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = restaurantController.deleteRestaurant(testId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restaurantService, times(1)).deleteRestaurant(testId);
    }

    @Test
    void deleteRestaurant_whenRestaurantDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(restaurantService.deleteRestaurant(nonExistentId)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = restaurantController.deleteRestaurant(nonExistentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(restaurantService, times(1)).deleteRestaurant(nonExistentId);
    }
}
