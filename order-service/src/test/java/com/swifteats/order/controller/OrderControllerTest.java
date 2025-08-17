package com.swifteats.order.controller;

import com.swifteats.order.domain.OrderStatus;
import com.swifteats.order.domain.PaymentMethod;
import com.swifteats.order.domain.PaymentStatus;
import com.swifteats.order.dto.OrderDTO;
import com.swifteats.order.dto.OrderItemDTO;
import com.swifteats.order.dto.OrderItemRequest;
import com.swifteats.order.dto.OrderRequest;
import com.swifteats.order.dto.OrderStatusUpdateRequest;
import com.swifteats.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private UUID testOrderId;
    private UUID testUserId;
    private UUID testRestaurantId;
    private UUID testDriverId;
    private UUID testMenuItemId;
    private OrderDTO testOrder;
    private OrderRequest testOrderRequest;
    private OrderItemRequest testOrderItemRequest;
    private OrderStatusUpdateRequest testStatusUpdateRequest;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testRestaurantId = UUID.randomUUID();
        testDriverId = UUID.randomUUID();
        testMenuItemId = UUID.randomUUID();

        // Set up test order item request
        testOrderItemRequest = new OrderItemRequest();
        testOrderItemRequest.setMenuItemId(testMenuItemId);
        testOrderItemRequest.setQuantity(2);
        testOrderItemRequest.setSpecialInstructions("No onions");

        // Set up test order request
        testOrderRequest = new OrderRequest();
        testOrderRequest.setUserId(testUserId);
        testOrderRequest.setRestaurantId(testRestaurantId);
        testOrderRequest.setDeliveryAddress("123 Test St, Test City");
        testOrderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testOrderRequest.setSpecialInstructions("Ring the doorbell twice");
        testOrderRequest.setItems(Arrays.asList(testOrderItemRequest));

        // Set up test order item
        OrderItemDTO testOrderItem = new OrderItemDTO();
        testOrderItem.setId(UUID.randomUUID());
        testOrderItem.setMenuItemId(testMenuItemId);
        testOrderItem.setName("Test Item");
        testOrderItem.setPrice(new BigDecimal("15.99"));
        testOrderItem.setQuantity(2);
        testOrderItem.setSpecialInstructions("No onions");

        // Set up test order
        testOrder = OrderDTO.builder()
                .id(testOrderId)
                .userId(testUserId)
                .restaurantId(testRestaurantId)
                .driverId(testDriverId)
                .deliveryAddress("123 Test St, Test City")
                .status(OrderStatus.ACCEPTED)
                .totalAmount(new BigDecimal("31.98"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(30))
                .specialInstructions("Ring the doorbell twice")
                .items(Arrays.asList(testOrderItem))
                .build();

        // Set up status update request
        testStatusUpdateRequest = new OrderStatusUpdateRequest();
        testStatusUpdateRequest.setStatus(OrderStatus.IN_TRANSIT);
        testStatusUpdateRequest.setNotes("Driver on the way");
    }

    @Test
    void createOrder_shouldCreateAndReturnOrder() {
        // Arrange
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<OrderDTO> response = orderController.createOrder(testOrderRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        OrderDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testOrder.getId(), responseBody.getId());
        assertEquals(testOrder.getUserId(), responseBody.getUserId());
        assertEquals(testOrder.getRestaurantId(), responseBody.getRestaurantId());
        assertEquals(testOrder.getStatus(), responseBody.getStatus());
        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void getUserOrders_shouldReturnPageOfOrders() {
        // Arrange
        List<OrderDTO> orders = Arrays.asList(testOrder);
        Page<OrderDTO> page = new PageImpl<>(orders);
        Pageable pageable = Pageable.unpaged();

        when(orderService.getUserOrders(eq(testUserId), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<OrderDTO>> response = orderController.getUserOrders(testUserId, pageable);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<OrderDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.getTotalElements());
        assertEquals(testOrder.getId(), responseBody.getContent().get(0).getId());
        verify(orderService, times(1)).getUserOrders(eq(testUserId), any(Pageable.class));
    }

    @Test
    void getOrderById_whenOrderExists_shouldReturnOrder() {
        // Arrange
        when(orderService.getOrderById(testOrderId)).thenReturn(Optional.of(testOrder));

        // Act
        ResponseEntity<OrderDTO> response = orderController.getOrderById(testOrderId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testOrderId, responseBody.getId());
        assertEquals(testUserId, responseBody.getUserId());
        verify(orderService, times(1)).getOrderById(testOrderId);
    }

    @Test
    void getOrderById_whenOrderDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(orderService.getOrderById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<OrderDTO> response = orderController.getOrderById(nonExistentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(orderService, times(1)).getOrderById(nonExistentId);
    }

    @Test
    void updateOrderStatus_whenOrderExists_shouldUpdateAndReturnOrder() {
        // Arrange
        when(orderService.updateOrderStatus(eq(testOrderId), any(OrderStatusUpdateRequest.class)))
                .thenReturn(Optional.of(testOrder));

        // Act
        ResponseEntity<OrderDTO> response = orderController.updateOrderStatus(testOrderId, testStatusUpdateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testOrderId, responseBody.getId());
        assertEquals(testOrder.getStatus(), responseBody.getStatus());
        verify(orderService, times(1)).updateOrderStatus(eq(testOrderId), any(OrderStatusUpdateRequest.class));
    }

    @Test
    void updateOrderStatus_whenOrderDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(orderService.updateOrderStatus(eq(nonExistentId), any(OrderStatusUpdateRequest.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<OrderDTO> response = orderController.updateOrderStatus(nonExistentId, testStatusUpdateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(orderService, times(1)).updateOrderStatus(eq(nonExistentId), any(OrderStatusUpdateRequest.class));
    }

    @Test
    void assignDriverToOrder_whenOrderExists_shouldAssignDriverAndReturnOrder() {
        // Arrange
        when(orderService.assignDriverToOrder(testOrderId, testDriverId)).thenReturn(Optional.of(testOrder));

        // Act
        ResponseEntity<OrderDTO> response = orderController.assignDriverToOrder(testOrderId, testDriverId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OrderDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testOrderId, responseBody.getId());
        assertEquals(testDriverId, responseBody.getDriverId());
        verify(orderService, times(1)).assignDriverToOrder(testOrderId, testDriverId);
    }

    @Test
    void assignDriverToOrder_whenOrderDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(orderService.assignDriverToOrder(nonExistentId, testDriverId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<OrderDTO> response = orderController.assignDriverToOrder(nonExistentId, testDriverId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(orderService, times(1)).assignDriverToOrder(nonExistentId, testDriverId);
    }
}
