package com.swifteats.order.service;

import com.swifteats.order.domain.*;
import com.swifteats.order.dto.*;
import com.swifteats.order.messaging.OrderPublisher;
import com.swifteats.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

        private final OrderRepository orderRepository;
        private final OrderPublisher orderPublisher;
        // In a real app, we'd have a RestTemplate or Feign client to call other
        // services
        // private final RestaurantServiceClient restaurantServiceClient;

        @Transactional
        public OrderDTO createOrder(OrderRequest orderRequest) {
                log.info("Creating new order for user: {}, restaurant: {}",
                                orderRequest.getUserId(), orderRequest.getRestaurantId());

                // In a real app, we would validate the restaurant and menu items here
                // by calling the restaurant service

                // Create a new order
                Order order = Order.builder()
                                .userId(orderRequest.getUserId())
                                .restaurantId(orderRequest.getRestaurantId())
                                .deliveryAddress(orderRequest.getDeliveryAddress())
                                .status(OrderStatus.PENDING)
                                .paymentMethod(orderRequest.getPaymentMethod())
                                .paymentStatus(PaymentStatus.PENDING)
                                .createdAt(LocalDateTime.now())
                                .specialInstructions(orderRequest.getSpecialInstructions())
                                .items(new ArrayList<>())
                                .statusHistory(new ArrayList<>())
                                .build();

                // Add order items
                List<OrderItem> orderItems = orderRequest.getItems().stream()
                                .map(itemRequest -> {
                                        // In a real app, we would get the item details from the restaurant service
                                        // For now, we'll just mock the data
                                        BigDecimal price = BigDecimal.valueOf(10.0); // Mock price

                                        OrderItem orderItem = OrderItem.builder()
                                                        .menuItemId(itemRequest.getMenuItemId())
                                                        .name("Menu Item " + itemRequest.getMenuItemId()) // Mock name
                                                        .price(price)
                                                        .quantity(itemRequest.getQuantity())
                                                        .specialInstructions(itemRequest.getSpecialInstructions())
                                                        .build();

                                        order.addOrderItem(orderItem);
                                        return orderItem;
                                })
                                .collect(Collectors.toList());

                // Calculate total amount
                BigDecimal totalAmount = orderItems.stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                order.setTotalAmount(totalAmount);

                // Add initial status history
                OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                                .status(OrderStatus.PENDING)
                                .timestamp(LocalDateTime.now())
                                .updatedBy("SYSTEM")
                                .notes("Order created")
                                .build();

                order.addStatusHistory(statusHistory);

                // Save order
                Order savedOrder = orderRepository.save(order);

                // Convert to DTO
                OrderDTO orderDTO = convertToDTO(savedOrder);

                // Publish to queue for async processing
                orderPublisher.publishNewOrder(orderDTO);

                return orderDTO;
        }

        @Transactional(readOnly = true)
        public Page<OrderDTO> getUserOrders(UUID userId, Pageable pageable) {
                log.info("Fetching orders for user: {}", userId);
                return orderRepository.findByUserId(userId, pageable)
                                .map(this::convertToDTO);
        }

        @Transactional(readOnly = true)
        public Optional<OrderDTO> getOrderById(UUID id) {
                log.info("Fetching order with ID: {}", id);
                return orderRepository.findById(id)
                                .map(this::convertToDTO);
        }

        @Transactional
        @CircuitBreaker(name = "orderService", fallbackMethod = "updateOrderStatusFallback")
        public Optional<OrderDTO> updateOrderStatus(UUID id, OrderStatusUpdateRequest request) {
                log.info("Updating order status for order ID: {} to {}", id, request.getStatus());

                return orderRepository.findById(id)
                                .map(order -> {
                                        // Update the order status
                                        order.setStatus(request.getStatus());

                                        // Add to status history
                                        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                                                        .status(request.getStatus())
                                                        .timestamp(LocalDateTime.now())
                                                        .updatedBy(request.getUpdatedBy() != null
                                                                        ? request.getUpdatedBy()
                                                                        : "SYSTEM")
                                                        .notes(request.getNotes())
                                                        .build();

                                        order.addStatusHistory(statusHistory);

                                        // Update completed time if delivered
                                        if (request.getStatus() == OrderStatus.DELIVERED) {
                                                order.setCompletedAt(LocalDateTime.now());
                                        }

                                        // Save order
                                        Order updatedOrder = orderRepository.save(order);

                                        // Convert to DTO
                                        OrderDTO orderDTO = convertToDTO(updatedOrder);

                                        // Publish status update
                                        orderPublisher.publishOrderStatusUpdate(orderDTO);

                                        return orderDTO;
                                });
        }

        @Transactional
        public Optional<OrderDTO> assignDriverToOrder(UUID orderId, UUID driverId) {
                log.info("Assigning driver {} to order {}", driverId, orderId);

                return orderRepository.findById(orderId)
                                .map(order -> {
                                        // Set driver ID
                                        order.setDriverId(driverId);

                                        // Update status to PICKED_UP if applicable
                                        if (order.getStatus() == OrderStatus.READY) {
                                                order.setStatus(OrderStatus.PICKED_UP);

                                                // Add to status history
                                                OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                                                                .status(OrderStatus.PICKED_UP)
                                                                .timestamp(LocalDateTime.now())
                                                                .updatedBy("SYSTEM")
                                                                .notes("Order picked up by driver")
                                                                .build();

                                                order.addStatusHistory(statusHistory);
                                        }

                                        // Save order
                                        Order updatedOrder = orderRepository.save(order);

                                        // Convert to DTO
                                        OrderDTO orderDTO = convertToDTO(updatedOrder);

                                        // Publish driver assignment
                                        orderPublisher.publishDriverAssignment(orderDTO);

                                        return orderDTO;
                                });
        }

        // Method called by the order consumer
        @Transactional
        public void processOrder(OrderDTO orderDTO) {
                log.info("Processing order from queue: {}", orderDTO.getId());

                // In a real system, we would:
                // 1. Process payment
                // 2. Notify restaurant
                // 3. Update order status

                // For now, just simulate accepting the order
                orderRepository.findById(orderDTO.getId())
                                .ifPresent(order -> {
                                        // Update status to ACCEPTED
                                        order.setStatus(OrderStatus.ACCEPTED);

                                        // Add to status history
                                        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                                                        .status(OrderStatus.ACCEPTED)
                                                        .timestamp(LocalDateTime.now())
                                                        .updatedBy("SYSTEM")
                                                        .notes("Order accepted")
                                                        .build();

                                        order.addStatusHistory(statusHistory);

                                        // Mock estimated delivery time (now + 45 minutes)
                                        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(45));

                                        // Update payment status to COMPLETED (mock payment)
                                        order.setPaymentStatus(PaymentStatus.COMPLETED);

                                        // Save order
                                        orderRepository.save(order);
                                });
        }

        // Fallback method for circuit breaker
        private Optional<OrderDTO> updateOrderStatusFallback(UUID id, OrderStatusUpdateRequest request, Exception ex) {
                log.error("Circuit breaker fallback: Error updating order status", ex);
                return Optional.empty();
        }

        // Helper methods for DTO conversion
        private OrderDTO convertToDTO(Order order) {
                List<OrderItemDTO> itemDTOs = order.getItems().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());

                return OrderDTO.builder()
                                .id(order.getId())
                                .userId(order.getUserId())
                                .restaurantId(order.getRestaurantId())
                                .driverId(order.getDriverId())
                                .deliveryAddress(order.getDeliveryAddress())
                                .status(order.getStatus())
                                .totalAmount(order.getTotalAmount())
                                .paymentMethod(order.getPaymentMethod())
                                .paymentStatus(order.getPaymentStatus())
                                .createdAt(order.getCreatedAt())
                                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                                .completedAt(order.getCompletedAt())
                                .specialInstructions(order.getSpecialInstructions())
                                .items(itemDTOs)
                                .build();
        }

        private OrderItemDTO convertToDTO(OrderItem orderItem) {
                return OrderItemDTO.builder()
                                .id(orderItem.getId())
                                .menuItemId(orderItem.getMenuItemId())
                                .name(orderItem.getName())
                                .price(orderItem.getPrice())
                                .quantity(orderItem.getQuantity())
                                .specialInstructions(orderItem.getSpecialInstructions())
                                .subtotal(orderItem.getSubtotal())
                                .build();
        }
}
