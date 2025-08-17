# API Gateway Documentation

The API Gateway serves as the entry point for all client requests in the SwiftEats application. It routes requests to the appropriate microservices, provides circuit breaking capabilities, and handles fallbacks when services are unavailable.

## Base URL

- `http://localhost:8080`

## Routing Configuration

The API Gateway routes requests to the following services:

| Path Pattern | Service | Port | Description |
|--------------|---------|------|-------------|
| `/api/orders/**` | Order Service | 8081 | Order creation and management |
| `/api/restaurants/**`, `/api/menus/**` | Restaurant Service | 8082 | Restaurant and menu management |
| `/api/drivers/**`, `/api/locations/**` | Driver Service | 8083 | Driver management and location tracking |
| `/api/simulator/**` | Simulator Service | 8084 | Simulation control for testing |

## Fallback Endpoints

When a service is unavailable, the API Gateway provides fallback responses:

### Order Service Fallback

```bash
curl -X GET http://localhost:8080/fallback/orders
```

Example response:
```json
{
  "status": "error",
  "message": "Order Service is currently unavailable. Please try again later."
}
```

### Restaurant Service Fallback

```bash
curl -X GET http://localhost:8080/fallback/restaurants
```

Example response:
```json
{
  "status": "error",
  "message": "Restaurant Service is currently unavailable. Please try again later."
}
```

### Driver Service Fallback

```bash
curl -X GET http://localhost:8080/fallback/drivers
```

Example response:
```json
{
  "status": "error",
  "message": "Driver Service is currently unavailable. Please try again later."
}
```

## Actuator Endpoints

The API Gateway exposes the following actuator endpoints for monitoring and management:

```bash
# Health check endpoint
curl -X GET http://localhost:8080/actuator/health

# Gateway routes information
curl -X GET http://localhost:8080/actuator/gateway/routes

# Metrics information
curl -X GET http://localhost:8080/actuator/metrics
```

## Configuration

The API Gateway is configured through `application.properties` with the following key settings:

- `server.port`: The port on which the gateway listens (8080)
- `spring.cloud.gateway.routes`: Route definitions for each service
- `spring.data.redis.host/port`: Redis configuration for caching
- `management.endpoints.web.exposure.include`: Exposed actuator endpoints

## Notes

1. The API Gateway uses Spring Cloud Gateway for routing.
2. All API requests should be directed to the gateway rather than individual services.
3. Authentication and authorization will be implemented at the gateway level (future enhancement).
4. The gateway provides service discovery via static configuration.
