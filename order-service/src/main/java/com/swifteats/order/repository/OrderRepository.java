package com.swifteats.order.repository;

import com.swifteats.order.domain.Order;
import com.swifteats.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    List<Order> findByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    List<Order> findByDriverId(UUID driverId);

    List<Order> findByDriverIdAndStatus(UUID driverId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<Order> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
