package com.swifteats.restaurant.service;

import com.swifteats.restaurant.domain.Restaurant;
import com.swifteats.restaurant.dto.RestaurantDTO;
import com.swifteats.restaurant.repository.RestaurantRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Cacheable(value = "restaurants", key = "'all'")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getAllRestaurantsFallback")
    public List<RestaurantDTO> getAllRestaurants() {
        log.info("Fetching all restaurants from database");
        return restaurantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "restaurants", key = "#id")
    public Optional<RestaurantDTO> getRestaurantById(UUID id) {
        log.info("Fetching restaurant with ID: {}", id);
        return restaurantRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Cacheable(value = "restaurants", key = "'cuisine_' + #cuisine")
    public List<RestaurantDTO> getRestaurantsByCuisine(String cuisine) {
        log.info("Fetching restaurants with cuisine: {}", cuisine);
        return restaurantRepository.findByCuisine(cuisine).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "restaurants", key = "'open'")
    public List<RestaurantDTO> getOpenRestaurants() {
        log.info("Fetching currently open restaurants");
        LocalTime now = LocalTime.now();
        return restaurantRepository.findOpenRestaurants(now).stream()
                .map(restaurant -> {
                    RestaurantDTO dto = convertToDTO(restaurant);
                    dto.setOpen(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = { "restaurants", "menus" }, allEntries = true)
    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO) {
        log.info("Creating new restaurant: {}", restaurantDTO.getName());
        Restaurant restaurant = convertToEntity(restaurantDTO);
        return convertToDTO(restaurantRepository.save(restaurant));
    }

    @Transactional
    @CacheEvict(value = { "restaurants", "menus" }, allEntries = true)
    public Optional<RestaurantDTO> updateRestaurant(UUID id, RestaurantDTO restaurantDTO) {
        log.info("Updating restaurant with ID: {}", id);
        return restaurantRepository.findById(id)
                .map(existingRestaurant -> {
                    updateEntityFromDTO(existingRestaurant, restaurantDTO);
                    return convertToDTO(restaurantRepository.save(existingRestaurant));
                });
    }

    @Transactional
    @CacheEvict(value = { "restaurants", "menus" }, allEntries = true)
    public boolean deleteRestaurant(UUID id) {
        log.info("Deleting restaurant with ID: {}", id);
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Fallback method for circuit breaker
    private List<RestaurantDTO> getAllRestaurantsFallback(Exception ex) {
        log.error("Circuit breaker fallback: Error fetching restaurants", ex);
        return List.of(); // Return an empty list as fallback
    }

    // Helper methods for DTO conversion
    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        LocalTime now = LocalTime.now();
        boolean isOpen = restaurant.isActive() &&
                restaurant.getOpeningTime() != null && restaurant.getClosingTime() != null &&
                (now.isAfter(restaurant.getOpeningTime()) && now.isBefore(restaurant.getClosingTime()));

        return RestaurantDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .cuisine(restaurant.getCuisine())
                .phoneNumber(restaurant.getPhoneNumber())
                .rating(restaurant.getRating())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .deliveryFee(restaurant.getDeliveryFee())
                .estimatedDeliveryTime(restaurant.getEstimatedDeliveryTime())
                .logoUrl(restaurant.getLogoUrl())
                .isActive(restaurant.isActive())
                .isOpen(isOpen)
                .build();
    }

    private Restaurant convertToEntity(RestaurantDTO dto) {
        return Restaurant.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .address(dto.getAddress())
                .cuisine(dto.getCuisine())
                .phoneNumber(dto.getPhoneNumber())
                .rating(dto.getRating())
                .openingTime(dto.getOpeningTime())
                .closingTime(dto.getClosingTime())
                .deliveryFee(dto.getDeliveryFee())
                .estimatedDeliveryTime(dto.getEstimatedDeliveryTime())
                .logoUrl(dto.getLogoUrl())
                .isActive(dto.isActive())
                .build();
    }

    private void updateEntityFromDTO(Restaurant entity, RestaurantDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setAddress(dto.getAddress());
        entity.setCuisine(dto.getCuisine());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setRating(dto.getRating());
        entity.setOpeningTime(dto.getOpeningTime());
        entity.setClosingTime(dto.getClosingTime());
        entity.setDeliveryFee(dto.getDeliveryFee());
        entity.setEstimatedDeliveryTime(dto.getEstimatedDeliveryTime());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setActive(dto.isActive());
    }
}
