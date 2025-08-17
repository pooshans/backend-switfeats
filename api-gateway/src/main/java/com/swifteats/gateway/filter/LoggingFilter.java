package com.swifteats.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();

        // Add request ID to headers
        ServerHttpRequest requestWithId = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        // Record start time
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());

        // Log request details
        log.info("Request: [{}] {} {}", requestId, request.getMethod(), request.getURI());

        return chain.filter(exchange.mutate().request(requestWithId).build())
                .then(Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME);
                    if (startTime != null) {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("Response: [{}] Status: {} Duration: {}ms",
                                requestId,
                                exchange.getResponse().getStatusCode(),
                                duration);
                    }
                }));
    }

    @Override
    public int getOrder() {
        // Set high precedence (low value)
        return -1;
    }
}
