# SwiftEats Architecture

## Overview

The SwiftEats platform is designed as a modular monolith with clear domain boundaries, containerized using Docker, and ready for a potential future split into microservices. This approach was chosen to balance development speed, operational simplicity, and the ability to meet the high performance and reliability requirements.

## Architecture Diagram

```
                             ┌─────────────────────┐
                             │                     │
                             │    API Gateway      │
                             │                     │
                             └─────────┬───────────┘
                                       │
                                       │
           ┌───────────────────────────┼───────────────────────────┐
           │                           │                           │
  ┌────────▼─────────┐      ┌─────────▼──────────┐      ┌─────────▼────────┐
  │                  │      │                    │      │                   │
  │  Order Service   │◄────►│ Restaurant Service │◄────►│  Driver Service   │
  │                  │      │                    │      │                   │
  └────────┬─────────┘      └─────────┬──────────┘      └─────────┬────────┘
           │                          │                           │
           │                          │                           │
  ┌────────▼─────────┐      ┌─────────▼──────────┐      ┌─────────▼────────┐
  │                  │      │                    │      │                   │
  │  Order Database  │      │Restaurant Database │      │ Driver Database   │
  │   (PostgreSQL)   │      │   (PostgreSQL)     │      │   (PostgreSQL)    │
  │                  │      │                    │      │                   │
  └──────────────────┘      └────────────────────┘      └───────────────────┘
           │                          │                           │
           │                          │                           │
           └────────────┐  ┌──────────┘                           │
                        │  │                                      │
              ┌─────────▼──▼────────┐                 ┌───────────▼────────┐
              │                     │                 │                     │
              │    Redis Cache      │◄────────────────►   RabbitMQ Queue    │
              │                     │                 │                     │
              └─────────────────────┘                 └─────────────────────┘
                                                               ▲
                                                               │
                                                      ┌────────┴────────┐
                                                      │                 │
                                                      │    Simulator    │
                                                      │                 │
                                                      └─────────────────┘
```

## Architectural Pattern Choice

We've chosen a **Modular Monolith with Domain-Driven Design (DDD)** approach for the following reasons:

1. **Development Efficiency**: A monolith is faster to develop initially and easier to debug compared to a distributed system.
2. **Operational Simplicity**: Fewer moving parts means easier deployment and monitoring.
3. **Transition Path**: The modular design with clear boundaries allows for a future transition to microservices if needed.
4. **Performance**: Avoids network overhead between services for frequent internal communications.
5. **Transaction Management**: Simplifies transactions that span multiple domains.

Each module is organized around a specific business capability (Orders, Restaurants, Drivers) with its own database schema, reducing coupling between components. The modules communicate through well-defined interfaces, making potential future decomposition into microservices easier.

## Technology Justification

### Core Technologies

1. **Spring Boot**: A mature, feature-rich framework for building production-grade Java applications with minimal setup.
   - Built-in support for RESTful APIs, dependency injection, and application monitoring
   - Extensive ecosystem of libraries and integrations
   - Excellent performance characteristics and industry adoption

2. **PostgreSQL**: A powerful, open-source object-relational database system.
   - ACID compliance ensures data integrity
   - Advanced indexing capabilities for fast querying
   - Strong support for complex data types and JSON
   - Excellent performance for both reads and writes

3. **Redis**: In-memory data structure store used for caching and real-time data storage.
   - Used for high-speed caching of restaurant menus to meet the 200ms P99 requirement
   - Powers the pub/sub mechanism for real-time driver location updates
   - Reduces database load for frequently accessed data

4. **RabbitMQ**: Message broker for reliable asynchronous processing.
   - Ensures reliable order processing even during peak loads
   - Decouples services for better resilience
   - Provides message persistence for system recovery
   - Handles backpressure during traffic spikes

5. **Docker/Docker Compose**: Containerization for consistent development and deployment.
   - Ensures consistency across environments
   - Simplifies dependency management
   - Makes horizontal scaling possible

### Key Components for Meeting Technical Requirements

#### 1. Reliable Order Processing at Scale (500 orders/minute)

- **RabbitMQ**: Queues incoming orders, ensuring they are processed reliably even during system peaks
- **Circuit Breakers**: Implemented using Resilience4j to isolate failures in non-critical components
- **Transaction Management**: Spring's transaction management ensures data consistency

#### 2. High-Performance Menu & Restaurant Browse (P99 < 200ms)

- **Redis Caching**: Restaurant menus and status are cached in Redis with appropriate TTL
- **Optimized Database Queries**: Carefully designed indices and query optimizations
- **Read Replicas**: Database architecture prepared for read replicas if needed

#### 3. Real-Time Location Tracking (2000 events/second)

- **Redis Pub/Sub**: For real-time propagation of location updates to interested clients
- **WebSocket Support**: Direct push notifications to clients for live updates
- **In-Memory Processing**: Location data processed in memory for speed
- **Data Simulator**: Capable of generating realistic load for testing

## Resilience Strategy

1. **Circuit Breakers**: Prevent cascading failures by failing fast when dependencies are unreliable
2. **Timeouts**: Explicit timeouts on all external calls
3. **Retries**: Automatic retry with backoff for transient failures
4. **Fallbacks**: Graceful degradation when services are unavailable
5. **Bulkheads**: Resource isolation to contain failures

## Scalability Strategy

1. **Horizontal Scaling**: All services designed to run multiple instances
2. **Database Partitioning**: Each domain has its own database
3. **Caching**: Aggressive caching to reduce database load
4. **Asynchronous Processing**: Non-critical operations performed asynchronously
5. **Resource Optimization**: Efficient use of computing resources

## Data Flow for Key Scenarios

### 1. Order Placement

1. Customer submits order via API Gateway
2. Order Service validates order and places it in RabbitMQ queue
3. Payment processing is mocked (in a production system, would integrate with payment gateway)
4. Order Service processes the queued order, updating the database
5. Notifications are sent to the restaurant and customer
6. Order status is updated and cached

### 2. Restaurant Menu Browsing

1. Customer requests restaurant menu via API Gateway
2. Request hits the Restaurant Service
3. Service checks Redis cache first
4. If not in cache, retrieves from database and updates cache
5. Returns menu data to client with < 200ms latency

### 3. Real-time Driver Location Updates

1. Driver's device sends location update (simulated by the Simulator for testing)
2. Update is received by Driver Service
3. Service stores the update in the database and publishes to Redis
4. Subscribed clients (customer apps) receive real-time updates
5. Location history is maintained for analytics

## Maintainability Considerations

1. **Modular Design**: Clear separation of concerns between modules
2. **Comprehensive Testing**: Unit, integration, and load tests
3. **Documentation**: Detailed API specifications and architecture documents
4. **Monitoring**: Health checks and metrics exposed via Spring Actuator
5. **Code Quality**: Follow best practices and industry standards
