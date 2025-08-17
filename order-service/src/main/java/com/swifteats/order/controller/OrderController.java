package com.swifteats.order.controller;

import com.swifteats.order.dto.OrderDTO;
import com.swifteats.order.dto.OrderRequest;
import com.swifteats.order.dto.OrderStatusUpdateRequest;
import com.swifteats.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("REST request to create order for user: {}", orderRequest.getUserId());
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getUserOrders(
            @RequestParam UUID userId,
            Pageable pageable) {
        log.info("REST request to get orders for user: {}", userId);
        return ResponseEntity.ok(orderService.getUserOrders(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        log.info("REST request to get order with ID: {}", id);
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        log.info("REST request to update order status for order ID: {} to {}", id, request.getStatus());
        return orderService.updateOrderStatus(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/driver/{driverId}")
    public ResponseEntity<OrderDTO> assignDriverToOrder(
            @PathVariable UUID id,
            @PathVariable UUID driverId) {
        log.info("REST request to assign driver {} to order {}", driverId, id);
        return orderService.assignDriverToOrder(id, driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
