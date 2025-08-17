package com.swifteats.restaurant.controller;

import com.swifteats.restaurant.dto.RestaurantDTO;
import com.swifteats.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        log.info("REST request to get all restaurants");
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable UUID id) {
        log.info("REST request to get restaurant with ID: {}", id);
        return restaurantService.getRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cuisine/{cuisine}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByCuisine(@PathVariable String cuisine) {
        log.info("REST request to get restaurants by cuisine: {}", cuisine);
        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisine(cuisine));
    }

    @GetMapping("/open")
    public ResponseEntity<List<RestaurantDTO>> getOpenRestaurants() {
        log.info("REST request to get open restaurants");
        return ResponseEntity.ok(restaurantService.getOpenRestaurants());
    }

    @PostMapping
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        log.info("REST request to create restaurant: {}", restaurantDTO.getName());
        return new ResponseEntity<>(restaurantService.createRestaurant(restaurantDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable UUID id,
            @RequestBody RestaurantDTO restaurantDTO) {
        log.info("REST request to update restaurant with ID: {}", id);
        return restaurantService.updateRestaurant(id, restaurantDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable UUID id) {
        log.info("REST request to delete restaurant with ID: {}", id);
        if (restaurantService.deleteRestaurant(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
