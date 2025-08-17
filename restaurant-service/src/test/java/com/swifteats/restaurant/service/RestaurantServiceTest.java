package com.swifteats.restaurant.service;

import com.swifteats.restaurant.domain.Restaurant;
import com.swifteats.restaurant.dto.RestaurantDTO;
import com.swifteats.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testRestaurant = Restaurant.builder()
                .id(testId)
                .name("Test Restaurant")
                .description("A test restaurant")
                .address("123 Test St")
                .cuisine("Italian")
                .phoneNumber("123-456-7890")
                .rating(4.5f)
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(22, 0))
                .isActive(true)
                .build();
    }

    @Test
    void getAllRestaurants() {
        // Arrange
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(testRestaurant));

        // Act
        List<RestaurantDTO> result = restaurantService.getAllRestaurants();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRestaurant.getName(), result.get(0).getName());
        verify(restaurantRepository).findAll();
    }

    @Test
    void getRestaurantById() {
        // Arrange
        when(restaurantRepository.findById(testId)).thenReturn(Optional.of(testRestaurant));

        // Act
        Optional<RestaurantDTO> result = restaurantService.getRestaurantById(testId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRestaurant.getName(), result.get().getName());
        verify(restaurantRepository).findById(testId);
    }

    @Test
    void createRestaurant() {
        // Arrange
        RestaurantDTO dto = new RestaurantDTO();
        dto.setName("New Restaurant");
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act
        RestaurantDTO result = restaurantService.createRestaurant(dto);

        // Assert
        assertNotNull(result);
        assertEquals(testRestaurant.getName(), result.getName());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void updateRestaurant() {
        // Arrange
        RestaurantDTO dto = new RestaurantDTO();
        dto.setName("Updated Restaurant");
        when(restaurantRepository.findById(testId)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // Act
        Optional<RestaurantDTO> result = restaurantService.updateRestaurant(testId, dto);

        // Assert
        assertTrue(result.isPresent());
        verify(restaurantRepository).findById(testId);
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void deleteRestaurant_whenExists() {
        // Arrange
        when(restaurantRepository.existsById(testId)).thenReturn(true);

        // Act
        boolean result = restaurantService.deleteRestaurant(testId);

        // Assert
        assertTrue(result);
        verify(restaurantRepository).deleteById(testId);
    }

    @Test
    void deleteRestaurant_whenNotExists() {
        // Arrange
        when(restaurantRepository.existsById(testId)).thenReturn(false);

        // Act
        boolean result = restaurantService.deleteRestaurant(testId);

        // Assert
        assertFalse(result);
        verify(restaurantRepository, never()).deleteById(any());
    }
}
