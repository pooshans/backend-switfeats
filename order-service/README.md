# Order Service API Documentation

This document provides examples of working API requests for the Order Service in the SwiftEats application.

## Base URLs

- Local: `http://localhost:8084/api/orders` (direct to service)
- Gateway: `http://localhost:8080/api/orders` (through API Gateway)

## Order Endpoints

### Create a New Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "restaurantId": "b12a86cb-bbca-4923-8fb3-f0e58c3d2402",
    "deliveryAddress": "123 Main St, Apt 4B, San Francisco, CA 94105",
    "paymentMethod": "CREDIT_CARD",
    "specialInstructions": "Please ring doorbell on arrival",
    "items": [
      {
        "menuItemId": "5fa85f64-5717-4562-b3fc-2c963f66afa6",
        "quantity": 2,
        "specialInstructions": "Extra sauce on the side"
      },
      {
        "menuItemId": "7fa85f64-5717-4562-b3fc-2c963f66afa7",
        "quantity": 1,
        "specialInstructions": "No onions please"
      }
    ]
  }'
```

### Get Orders for a User

```bash
curl -X GET "http://localhost:8080/api/orders?userId=3fa85f64-5717-4562-b3fc-2c963f66afa6&page=0&size=10"
```

### Get Order by ID

```bash
curl -X GET http://localhost:8080/api/orders/{order-id}
```

Example:
```bash
curl -X GET http://localhost:8080/api/orders/a04449e0-9b05-4a4c-bd80-6238f51060f5
```

### Update Order Status

```bash
curl -X PUT http://localhost:8080/api/orders/{order-id}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PREPARING",
    "notes": "Order is being prepared in the kitchen",
    "updatedBy": "Restaurant Staff"
  }'
```

Example:
```bash
curl -X PUT http://localhost:8080/api/orders/a04449e0-9b05-4a4c-bd80-6238f51060f5/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "READY",
    "notes": "Order is ready for pickup",
    "updatedBy": "Kitchen Manager"
  }'
```

### Assign Driver to Order

```bash
curl -X PUT http://localhost:8080/api/orders/{order-id}/driver/{driver-id}
```

Example:
```bash
curl -X PUT http://localhost:8080/api/orders/a04449e0-9b05-4a4c-bd80-6238f51060f5/driver/123e4567-e89b-12d3-a456-426614174000
```

## Field Descriptions

### Order Request

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| userId | UUID | ID of the user placing the order | Yes |
| restaurantId | UUID | ID of the restaurant for the order | Yes |
| items | Array | List of items in the order | Yes |
| deliveryAddress | String | Full delivery address | Yes |
| paymentMethod | Enum | Payment method (see below) | Yes |
| specialInstructions | String | Special instructions for the order | No |

### Order Item Request

| Field | Type | Description | Required |
|-------|------|-------------|----------|
| menuItemId | UUID | ID of the menu item | Yes |
| quantity | Integer | Quantity of the item (min: 1) | Yes |
| specialInstructions | String | Special instructions for this item | No |

### Order DTO (Response)

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique order identifier |
| userId | UUID | ID of the user who placed the order |
| restaurantId | UUID | ID of the restaurant |
| driverId | UUID | ID of the assigned driver (if any) |
| deliveryAddress | String | Full delivery address |
| status | Enum | Current order status (see below) |
| totalAmount | BigDecimal | Total order amount |
| paymentMethod | Enum | Payment method used |
| paymentStatus | Enum | Current payment status |
| createdAt | LocalDateTime | When the order was created |
| estimatedDeliveryTime | LocalDateTime | Estimated delivery time |
| completedAt | LocalDateTime | When the order was completed |
| specialInstructions | String | Special instructions for the order |
| items | Array | List of items in the order |

## Enumerations

### Order Status

- `PENDING`: Order received but not yet accepted by the restaurant
- `ACCEPTED`: Order accepted by the restaurant
- `PREPARING`: Food is being prepared
- `READY`: Order is ready for pickup
- `PICKED_UP`: Driver has picked up the order
- `IN_TRANSIT`: Driver is delivering the order
- `DELIVERED`: Order has been delivered
- `CANCELLED`: Order was cancelled

### Payment Method

- `CREDIT_CARD`: Payment via credit card
- `DEBIT_CARD`: Payment via debit card
- `WALLET`: Payment via digital wallet
- `CASH_ON_DELIVERY`: Payment in cash upon delivery

### Payment Status

- `PENDING`: Payment is pending
- `COMPLETED`: Payment has been completed
- `FAILED`: Payment has failed
- `REFUNDED`: Payment has been refunded

## Testing Order Flow

To test a complete order flow, follow these steps:

1. **Create an Order** using the POST endpoint
2. **Restaurant Accepts the Order** by updating status to ACCEPTED
3. **Restaurant Prepares the Order** by updating status to PREPARING
4. **Restaurant Marks Order as Ready** by updating status to READY
5. **Assign a Driver** to the order using the driver assignment endpoint
6. **Driver Picks Up the Order** by updating status to PICKED_UP
7. **Driver Marks Order In Transit** by updating status to IN_TRANSIT
8. **Driver Marks Order as Delivered** by updating status to DELIVERED

## Notes

1. All timestamp fields use ISO 8601 format.
2. The order service uses JSR310 for proper date/time serialization and deserialization.
3. Authentication and authorization requirements are not included in these examples.
4. The order flow typically involves multiple status updates as the order progresses.
5. When testing the complete flow, ensure you have valid restaurant and driver IDs.
