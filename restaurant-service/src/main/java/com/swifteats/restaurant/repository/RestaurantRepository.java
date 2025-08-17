package com.swifteats.restaurant.repository;

import com.swifteats.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByCuisine(String cuisine);

    List<Restaurant> findByIsActiveTrue();

    @Query("SELECT r FROM Restaurant r WHERE r.openingTime <= ?1 AND r.closingTime >= ?1 AND r.isActive = true")
    List<Restaurant> findOpenRestaurants(LocalTime currentTime);

    List<Restaurant> findByNameContainingIgnoreCase(String name);
}
