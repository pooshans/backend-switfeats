package com.swifteats.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order_service_route", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .rewritePath("/api/v1/orders/(?<segment>.*)", "/api/v1/orders/${segment}"))
                        .uri("http://order-service:8081"))
                .route("restaurant_service_route", r -> r
                        .path("/api/v1/restaurants/**", "/api/v1/menus/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("restaurantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/restaurants"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver())))
                        .uri("http://restaurant-service:8082"))
                .route("driver_service_route", r -> r
                        .path("/api/v1/drivers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("driverServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/drivers"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver())))
                        .uri("http://driver-service:8083"))
                .route("websocket_route", r -> r
                        .path("/ws/**")
                        .uri("http://driver-service:8083"))
                .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200); // 100 requests per second, 200 burst capacity
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}
