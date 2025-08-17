# Restaurant Service API Documentation

This document provides examples of working API requests for the Restaurant Service in the SwiftEats application.

## Base URLs

- Local: `http://localhost:8082/api/restaurants` (direct to service)
- Gateway: `http://localhost:8080/api/restaurants` (through API Gateway)

## Restaurant Endpoints

### Get All Restaurants

```bash
curl -X GET http://localhost:8080/api/restaurants
```

### Get Restaurant by ID

```bash
curl -X GET http://localhost:8080/api/restaurants/{restaurant-id}
```

Example:
```bash
curl -X GET http://localhost:8080/api/restaurants/b12a86cb-bbca-4923-8fb3-f0e58c3d2402
```

### Get Restaurants by Cuisine

```bash
curl -X GET http://localhost:8080/api/restaurants/cuisine/{cuisine-type}
```

Example:
```bash
curl -X GET http://localhost:8080/api/restaurants/cuisine/Italian
```

### Get Open Restaurants

```bash
curl -X GET http://localhost:8080/api/restaurants/open
```

### Create a New Restaurant

```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Restaurant Name",
    "address": "123 Main St",
    "phoneNumber": "555-1234",
    "cuisine": "Italian",
    "rating": 4.5,
    "deliveryFee": 3.99,
    "estimatedDeliveryTime": 30,
    "isActive": true,
    "openingTime": "08:00:00",
    "closingTime": "22:00:00"
  }'
```

Example:
```bash
curl -X POST http://localhost:8080/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Italian Delight",
    "address": "123 Main St",
    "phoneNumber": "555-1234",
    "cuisine": "Italian",
    "rating": 4.5,
    "deliveryFee": 3.99,
    "estimatedDeliveryTime": 30
  }'
```

### Update an Existing Restaurant

```bash
curl -X PUT http://localhost:8080/api/restaurants/{restaurant-id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Restaurant Name",
    "address": "123 Main St",
    "phoneNumber": "555-1234",
    "cuisine": "Italian",
    "rating": 4.8,
    "deliveryFee": 2.99,
    "estimatedDeliveryTime": 25,
    "isActive": true
  }'
```

Example:
```bash
curl -X PUT http://localhost:8080/api/restaurants/b12a86cb-bbca-4923-8fb3-f0e58c3d2402 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Italian Delight",
    "address": "123 Main St",
    "phoneNumber": "555-1234",
    "cuisine": "Italian",
    "rating": 4.8,
    "deliveryFee": 2.99,
    "estimatedDeliveryTime": 25,
    "isActive": true
  }'
```

### Delete a Restaurant

```bash
curl -X DELETE http://localhost:8080/api/restaurants/{restaurant-id}
```

Example:
```bash
curl -X DELETE http://localhost:8080/api/restaurants/a04449e0-9b05-4a4c-bd80-6238f51060f5
```

## Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| name | String | Name of the restaurant |
| description | String | Description of the restaurant |
| address | String | Physical address |
| cuisine | String | Type of cuisine (e.g., Italian, Chinese) |
| phoneNumber | String | Contact number |
| rating | Float | Restaurant rating (0-5) |
| openingTime | Time | Restaurant opening time (HH:MM:SS) |
| closingTime | Time | Restaurant closing time (HH:MM:SS) |
| deliveryFee | Double | Fee charged for delivery |
| estimatedDeliveryTime | Integer | Estimated delivery time in minutes |
| logoUrl | String | URL to restaurant logo image |
| isActive | Boolean | Whether the restaurant is active in the system |
| isOpen | Boolean | Whether the restaurant is currently open (calculated based on opening/closing times) |

## Notes

1. All timestamp fields use ISO 8601 format.
2. Authentication and authorization requirements are not included in these examples.
3. When using boolean fields with "is" prefix (like `isActive`), make sure to include the full field name in your JSON.
