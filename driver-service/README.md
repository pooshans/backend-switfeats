# Driver Service API Documentation

This document provides examples of working API requests for the Driver Service in the SwiftEats application.

## Base URLs

- Local: `http://localhost:8083/api/v1/drivers` (direct to service)
- Gateway: `http://localhost:8080/api/v1/drivers` (through API Gateway)

## Driver Endpoints

### Get All Drivers

```bash
curl -X GET http://localhost:8080/api/v1/drivers
```

### Get Driver by ID

```bash
curl -X GET http://localhost:8080/api/v1/drivers/{driver-id}
```

Example:
```bash
curl -X GET http://localhost:8080/api/v1/drivers/123e4567-e89b-12d3-a456-426614174000
```

### Create a New Driver

```bash
curl -X POST http://localhost:8080/api/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "555-1234",
    "licenseNumber": "DL12345678",
    "vehicleType": "CAR",
    "vehiclePlate": "ABC123",
    "status": "AVAILABLE"
  }'
```

### Update Driver Information

```bash
curl -X PUT http://localhost:8080/api/v1/drivers/{driver-id} \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phoneNumber": "555-5678",
    "licenseNumber": "DL12345678",
    "vehicleType": "CAR",
    "vehiclePlate": "ABC123",
    "status": "AVAILABLE"
  }'
```

### Update Driver Status

```bash
curl -X PATCH http://localhost:8080/api/v1/drivers/{driver-id}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "BUSY"
  }'
```

Example:
```bash
curl -X PATCH http://localhost:8080/api/v1/drivers/123e4567-e89b-12d3-a456-426614174000/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "BUSY"
  }'
```

### Delete a Driver

```bash
curl -X DELETE http://localhost:8080/api/v1/drivers/{driver-id}
```

## Driver Location Endpoints

### Update Driver Location

```bash
curl -X POST http://localhost:8080/api/v1/drivers/location \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": "123e4567-e89b-12d3-a456-426614174000",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "timestamp": "2025-08-13T10:15:30Z"
  }'
```

### Batch Update Driver Locations

```bash
curl -X POST http://localhost:8080/api/v1/drivers/location/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "driverId": "123e4567-e89b-12d3-a456-426614174000",
      "latitude": 37.7749,
      "longitude": -122.4194,
      "timestamp": "2025-08-13T10:15:30Z"
    },
    {
      "driverId": "223e4567-e89b-12d3-a456-426614174001",
      "latitude": 37.7750,
      "longitude": -122.4195,
      "timestamp": "2025-08-13T10:15:30Z"
    }
  ]'
```

### Get Driver's Current Location

```bash
curl -X GET http://localhost:8080/api/v1/drivers/{driver-id}/location
```

Example:
```bash
curl -X GET http://localhost:8080/api/v1/drivers/123e4567-e89b-12d3-a456-426614174000/location
```

### Get Driver's Location History

```bash
curl -X GET http://localhost:8080/api/v1/drivers/{driver-id}/location/history
```

Example:
```bash
curl -X GET http://localhost:8080/api/v1/drivers/123e4567-e89b-12d3-a456-426614174000/location/history
```

### Get Nearby Drivers

```bash
curl -X GET "http://localhost:8080/api/v1/drivers/nearby?latitude=37.7749&longitude=-122.4194&radius=5.0"
```

## Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| firstName | String | Driver's first name |
| lastName | String | Driver's last name |
| email | String | Driver's email address |
| phoneNumber | String | Driver's contact number |
| licenseNumber | String | Driver's license number |
| vehicleType | String | Type of vehicle (CAR, MOTORCYCLE, BICYCLE) |
| vehiclePlate | String | Vehicle license plate number |
| status | String | Driver's current status (AVAILABLE, BUSY, OFFLINE) |
| currentLocation | LocationDTO | Driver's current location data |
| createdAt | LocalDateTime | When the driver record was created |
| updatedAt | LocalDateTime | When the driver record was last updated |

### Location Fields

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| driverId | UUID | ID of the associated driver |
| latitude | Double | Latitude coordinate |
| longitude | Double | Longitude coordinate |
| timestamp | LocalDateTime | When the location was recorded |

## Notes

1. All timestamp fields use ISO 8601 format (e.g., "2025-08-13T10:15:30Z").
2. The driver service uses JSR310 for proper date/time serialization and deserialization.
3. Authentication and authorization requirements are not included in these examples.
4. Driver status can be one of: AVAILABLE, BUSY, OFFLINE.
5. Vehicle type can be one of: CAR, MOTORCYCLE, BICYCLE.
