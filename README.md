# SwiftEats

A scalable, resilient, and high-performance backend for a modern food delivery service platform. SwiftEats is designed to handle high order volumes, provide real-time driver location tracking, and ensure a fast restaurant browsing experience.

## Overview

SwiftEats is a food delivery platform that connects customers, restaurants, and drivers in Maharashtra. The platform is built with Spring Boot and containerized with Docker, using PostgreSQL for persistence, Redis for caching and real-time updates, and RabbitMQ for reliable order processing.

## Features

- **Order Processing**: Handles up to 500 orders/minute with queue-based processing
- **Restaurant Browsing**: High-performance menu viewing with < 200ms response times
- **Real-time Driver Tracking**: Live tracking of driver locations via WebSocket
- **Modular Design**: Clear separation of concerns for maintainability
- **Resilient Architecture**: Circuit breakers, timeouts, and fallbacks for reliability
- **Scalable Infrastructure**: Containerized services ready for horizontal scaling

## Technical Stack

- **Backend**: Java 21, Spring Boot 3.2.0
- **Database**: PostgreSQL 16
- **Caching & Real-time Updates**: Redis 7
- **Message Queue**: RabbitMQ 3
- **Containerization**: Docker & Docker Compose
- **API Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, JaCoCo for coverage

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven
- Git

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/swifteats.git
cd swifteats
```

### Build the Project

```bash
# Build all services
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# To see logs
docker-compose logs -f

# To stop all services
docker-compose down
```

### Access Services

- **API Gateway**: http://localhost:8080
- **Order Service**: http://localhost:8081
- **Restaurant Service**: http://localhost:8082
- **Driver Service**: http://localhost:8083
- **Simulator**: http://localhost:8084
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### Running the Simulator

The simulator will automatically start generating driver location data when the application starts. By default, it will simulate 50 drivers sending updates every 5 seconds.

You can control the simulator through its REST API:

```bash
# Start simulation
curl -X POST http://localhost:8084/api/simulator/start

# Stop simulation
curl -X POST http://localhost:8084/api/simulator/stop

# Change number of drivers
curl -X PUT http://localhost:8084/api/simulator/config -H "Content-Type: application/json" -d '{"driverCount": 30, "updateFrequencyMs": 5000}'
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Generate Coverage Report

```bash
mvn verify
```

The coverage report will be available in the `target/site/jacoco` directory of each service.

## Documentation

- **Architecture**: See `ARCHITECTURE.md` for detailed system design
- **Project Structure**: See `PROJECT_STRUCTURE.md` for code organization
- **API Specification**: See `API-SPECIFICATION.yml` for endpoint details
- **Development History**: See `CHAT_HISTORY.md` for AI collaboration details

## License

This project is licensed under the MIT License - see the LICENSE file for details.
