package com.swifteats.restaurant.repository;

import com.swifteats.restaurant.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantId(UUID restaurantId);

    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(UUID restaurantId);

    List<MenuItem> findByCategory(String category);

    List<MenuItem> findByRestaurantIdAndCategory(UUID restaurantId, String category);
}
