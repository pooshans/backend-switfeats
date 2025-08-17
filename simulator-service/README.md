# Simulator Service API Documentation

This document provides examples of API requests for the Simulator Service in the SwiftEats application. The simulator service is used to generate realistic testing data for the application, particularly focused on simulating driver movements and location updates.

## Base URLs

- Local: `http://localhost:8090/api/simulator` (direct to service)
- Docker: `http://simulator-service:8090/api/simulator` (within Docker network)
- Via API Gateway: `http://localhost:8080/api/simulator` (through API Gateway)

## Simulator Endpoints

### Get Simulation Status

Returns the current status of the simulation, including whether it's active, how many drivers are being simulated, and the update interval.

```bash
curl -X GET http://localhost:8090/api/simulator/status
```

Example response:
```json
{
  "active": true,
  "driverCount": 10000,
  "updateIntervalMs": 500,
  "updatesPerSecond": 20000.0
}
```

### Restart Simulation

Restarts the simulation with the configured number of drivers.

```bash
curl -X POST http://localhost:8090/api/simulator/control/restart
```

Example response:
```json
{
  "status": "success",
  "message": "Simulation restarted with 10000 drivers"
}
```

### Set Driver Count

Updates the number of simulated drivers. The count must be between 1 and 50000.

```bash
curl -X POST "http://localhost:8090/api/simulator/control/driver-count?count=1000"
```

Example response:
```json
{
  "status": "success",
  "message": "Driver count updated to 1000"
}
```

Example error response (when count is invalid):
```json
{
  "status": "error",
  "message": "Driver count must be between 1 and 50000"
}
```

```

## Location Update Flow

The Simulator Service automatically generates simulated driver movements and sends location updates to the Driver Service through the API Gateway. This is an automated process that happens in the background and doesn't require API calls to trigger it.

### Customizing Location Simulation

The simulator generates driver movements within the boundaries defined in the code. These can be modified by updating the following constants in `SimulationService.java`:

```java
// San Francisco coordinates for simulation
private static final double SF_LAT_MIN = 37.7;
private static final double SF_LAT_MAX = 37.8;
private static final double SF_LNG_MIN = -122.5;
private static final double SF_LNG_MAX = -122.4;
```

To simulate a different geographic area, these values can be updated to match the desired latitude and longitude boundaries.

### Technical Implementation

The location update process works as follows:

1. The Simulator Service initializes a configurable number of simulated drivers (default: 10).
2. At regular intervals (default: 500ms), the service updates the position of each driver.
3. The updated locations are sent to the Driver Service via the API Gateway using the endpoint: `POST /api/v1/drivers/location/batch`.
4. The Driver Service processes these updates and stores them for use by other services.

### Location Update Data Format

```json
[
  {
    "driverId": 1,
    "latitude": 37.75482,
    "longitude": -122.46328,
    "heading": 245.8,
    "speed": 35.4,
    "accuracy": 5.2
  },
  {
    "driverId": 2,
    "latitude": 37.76391,
    "longitude": -122.41927,
    "heading": 112.3,
    "speed": 28.6,
    "accuracy": 4.8
  }
]
```

## Simulator Configuration

The simulator can be configured through the following properties in `application.properties` or via environment variables:

| Property | Description | Default | Environment Variable |
|----------|-------------|---------|---------------------|
| simulator.driver.count | Number of drivers to simulate | 10 | SIMULATOR_DRIVER_COUNT |
| simulator.update.interval | Update interval in milliseconds | 500 | SIMULATOR_UPDATE_INTERVAL |
| simulator.api.url | URL for the API Gateway | http://api-gateway:8080 | SIMULATOR_API_URL |

## Docker Configuration

When running in Docker, the service can be configured using the following environment variables:

```bash
docker run -d --name simulator-service \
  --network=swifteats_default \
  -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SIMULATOR_API_URL=http://api-gateway:8080 \
  -e SIMULATOR_DRIVER_COUNT=10 \
  swifteats-simulator-service
```

## Notes

1. The simulator is primarily used for testing and development purposes.
2. Driver locations are randomly generated within a geographic boundary (San Francisco area by default).
3. Each driver's movement follows realistic patterns with gradual direction and speed changes.
4. The simulation can generate a significant load on the system when using a high driver count and low update interval.
5. For production environments, it's recommended to disable the simulator service or run it with a very limited number of drivers.
6. The service now correctly routes requests through the API Gateway at the configured URL.
7. Batch location updates are used to reduce network overhead when sending multiple driver locations at once.

## Building and Deploying Changes

After making changes to the code, you can rebuild and redeploy the service using the following commands:

```bash
# Build the JAR file
cd /path/to/swifteats
mvn clean package -DskipTests -pl simulator-service

# Build the Docker image
docker build -t swifteats-simulator-service ./simulator-service

# Stop and remove any existing container
docker stop swifteats-simulator-service-1 && docker rm swifteats-simulator-service-1

# Start a new container with the updated image
docker run -d --name swifteats-simulator-service-1 \
  --network=swifteats_default \
  -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SIMULATOR_API_URL=http://api-gateway:8080 \
  -e SIMULATOR_DRIVER_COUNT=10 \
  swifteats-simulator-service
```

## Troubleshooting

### Location Updates Not Being Sent

If the Driver Service is not receiving location updates, check the following:

1. **API Gateway Connection**: Ensure the `SIMULATOR_API_URL` is correctly set to the API Gateway URL. Check the logs for connection errors.

   ```bash
   docker logs swifteats-simulator-service-1 | grep -i error
   ```

2. **API Gateway Routes**: Verify that the API Gateway has the correct routes configured for the Driver Service:

   ```properties
   # In api-gateway/src/main/resources/application.properties
   spring.cloud.gateway.routes[2].id=driver-service
   spring.cloud.gateway.routes[2].uri=http://driver-service:8083
   spring.cloud.gateway.routes[2].predicates[0]=Path=/api/drivers/**,/api/locations/**
   ```

3. **Driver Service Status**: Ensure the Driver Service is running and accessible:

   ```bash
   docker ps | grep driver-service
   ```

4. **Data Format**: If the API Gateway is routing correctly but the Driver Service is returning 400 Bad Request errors, check that the data format matches what the Driver Service expects. The `LocationUpdate` class should have the following fields:
   - `driverId` (Long)
   - `latitude` (Double)
   - `longitude` (Double)
   - `heading` (Double)
   - `speed` (Double)
   - `accuracy` (Double)

5. **Network Issues**: Check that all services are on the same Docker network:

   ```bash
   docker network inspect swifteats_default
   ```

## Monitoring and Logging

### Viewing Logs

To view the simulator service logs:

```bash
docker logs -f swifteats-simulator-service-1
```

### Health Check

The service provides health and metrics endpoints through Spring Boot Actuator:

```bash
# Health check
curl http://localhost:8090/actuator/health

# Metrics
curl http://localhost:8090/actuator/metrics

# Info
curl http://localhost:8090/actuator/info
```

### Performance Monitoring

When running with a large number of simulated drivers, monitor the CPU and memory usage:

```bash
docker stats swifteats-simulator-service-1
```
