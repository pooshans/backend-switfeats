package com.swifteats.order.domain;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
