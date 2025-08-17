# SwiftEats Project Structure

## Overview

The SwiftEats project is organized as a multi-module Maven project with each module representing a specific business domain or technical concern. This structure supports the Modular Monolith architecture described in `ARCHITECTURE.md`.

## Root Directory Structure

```
swifteats/
├── api-gateway/              # API Gateway service
├── order-service/            # Order management service
├── restaurant-service/       # Restaurant and menu management service
├── driver-service/           # Driver location and tracking service
├── simulator/                # GPS data simulator for testing
├── common/                   # Shared code and DTOs (optional, for future use)
├── tests/                    # Integration and end-to-end tests
├── docker-compose.yml        # Docker composition for all services
├── init-db.sh                # Database initialization script
├── pom.xml                   # Root Maven project file
├── README.md                 # Project overview and setup instructions
├── ARCHITECTURE.md           # Detailed architecture documentation
├── PROJECT_STRUCTURE.md      # This file
├── API-SPECIFICATION.yml     # OpenAPI specification
└── CHAT_HISTORY.md           # AI collaboration documentation
```

## Service Module Structure

Each service module follows a similar structure based on Spring Boot conventions:

### Order Service

```
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/swifteats/order/
│   │   │   ├── config/                # Configuration classes
│   │   │   ├── controller/            # REST controllers
│   │   │   ├── domain/                # Domain entities
│   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── exception/             # Custom exceptions
│   │   │   ├── repository/            # Data access layer
│   │   │   ├── service/               # Business logic
│   │   │   ├── messaging/             # RabbitMQ producers/consumers
│   │   │   ├── util/                  # Utility classes
│   │   │   └── OrderServiceApplication.java  # Main application class
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── data.sql                # Initial data script (if needed)
│   └── test/
│       └── java/com/swifteats/order/
│           ├── controller/             # Controller tests
│           ├── service/                # Service tests
│           ├── repository/             # Repository tests
│           └── integration/            # Integration tests
├── Dockerfile                          # Docker build instructions
└── pom.xml                             # Maven build file
```

### Restaurant Service

```
restaurant-service/
├── src/
│   ├── main/
│   │   ├── java/com/swifteats/restaurant/
│   │   │   ├── config/                # Configuration classes
│   │   │   ├── controller/            # REST controllers
│   │   │   ├── domain/                # Domain entities
│   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── exception/             # Custom exceptions
│   │   │   ├── repository/            # Data access layer
│   │   │   ├── service/               # Business logic
│   │   │   ├── cache/                 # Redis caching logic
│   │   │   ├── util/                  # Utility classes
│   │   │   └── RestaurantServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── data.sql                # Initial data script
│   └── test/
│       └── java/com/swifteats/restaurant/
│           ├── controller/             # Controller tests
│           ├── service/                # Service tests
│           ├── repository/             # Repository tests
│           └── integration/            # Integration tests
├── Dockerfile                          # Docker build instructions
└── pom.xml                             # Maven build file
```

### Driver Service

```
driver-service/
├── src/
│   ├── main/
│   │   ├── java/com/swifteats/driver/
│   │   │   ├── config/                # Configuration classes
│   │   │   ├── controller/            # REST controllers
│   │   │   ├── domain/                # Domain entities
│   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── exception/             # Custom exceptions
│   │   │   ├── repository/            # Data access layer
│   │   │   ├── service/               # Business logic
│   │   │   ├── messaging/             # RabbitMQ producers/consumers
│   │   │   ├── websocket/             # WebSocket configuration and handlers
│   │   │   ├── util/                  # Utility classes
│   │   │   └── DriverServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── data.sql                # Initial data script
│   └── test/
│       └── java/com/swifteats/driver/
│           ├── controller/             # Controller tests
│           ├── service/                # Service tests
│           ├── repository/             # Repository tests
│           └── integration/            # Integration tests
├── Dockerfile                          # Docker build instructions
└── pom.xml                             # Maven build file
```

### API Gateway

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/swifteats/gateway/
│   │   │   ├── config/                # Configuration classes
│   │   │   ├── filter/                # Gateway filters
│   │   │   ├── fallback/              # Circuit breaker fallbacks
│   │   │   ├── exception/             # Error handling
│   │   │   └── ApiGatewayApplication.java
│   │   └── resources/
│   │       └── application.properties  # Gateway configuration
│   └── test/
│       └── java/com/swifteats/gateway/
│           └── integration/            # Integration tests
├── Dockerfile                          # Docker build instructions
└── pom.xml                             # Maven build file
```

### Simulator

```
simulator/
├── src/
│   ├── main/
│   │   ├── java/com/swifteats/simulator/
│   │   │   ├── config/                # Configuration classes
│   │   │   ├── model/                 # Data models
│   │   │   ├── service/               # Simulation logic
│   │   │   ├── generator/             # Data generators
│   │   │   ├── controller/            # Control API
│   │   │   └── SimulatorApplication.java
│   │   └── resources/
│   │       └── application.properties  # Simulator configuration
│   └── test/
│       └── java/com/swifteats/simulator/
│           └── service/                # Service tests
├── Dockerfile                          # Docker build instructions
└── pom.xml                             # Maven build file
```

## Key Modules and Responsibilities

### Order Service
- Order processing and lifecycle management
- Payment processing (mocked)
- Order status updates
- Integration with restaurant service
- Communication with driver service for delivery

### Restaurant Service
- Restaurant information management
- Menu management
- Restaurant availability status
- High-performance caching for menu browsing
- Order acceptance and preparation status updates

### Driver Service
- Driver management
- Real-time location tracking
- Order assignment and delivery tracking
- WebSocket support for live updates
- Location history for analytics

### API Gateway
- Request routing
- Load balancing
- Request/response transformation
- Circuit breaking
- Rate limiting

### Simulator
- Generates simulated driver GPS data
- Configurable for different test scenarios
- Can simulate up to 50 drivers sending events every 5 seconds
- REST API to control simulation parameters

## Database Schema Organization

Each service has its own database schema to maintain isolation:

1. **orderdb**: Stores order data, payment information, and order status history
2. **restaurantdb**: Stores restaurant profiles, menus, and availability information
3. **driverdb**: Stores driver profiles, location history, and delivery assignments

## Shared Infrastructure

- **PostgreSQL**: Central relational database with separate schemas per service
- **Redis**: Used for caching and pub/sub messaging
- **RabbitMQ**: Used for asynchronous message processing

## Testing Strategy

- **Unit Tests**: Located in each service module, testing individual components
- **Integration Tests**: Testing interactions between components within a service
- **End-to-End Tests**: Located in the `/tests` directory, testing complete flows across services
