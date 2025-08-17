package com.swifteats.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoggingFilterTest {

    private LoggingFilter loggingFilter;
    private GatewayFilterChain filterChain;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        filterChain = mock(GatewayFilterChain.class);

        // Create a mock request
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, URI.create("http://localhost:8080/api/test"))
                .build();

        // Create a mock exchange with the request
        exchange = MockServerWebExchange.from(request);

        // Mock the filter chain to return an empty Mono
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void filter_shouldAddRequestIdHeader() {
        // Act
        Mono<Void> result = loggingFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify the request was modified with an X-Request-ID header
        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(exchangeCaptor.capture());

        ServerHttpRequest modifiedRequest = exchangeCaptor.getValue().getRequest();
        assertTrue(modifiedRequest.getHeaders().containsKey("X-Request-ID"));
        assertNotNull(modifiedRequest.getHeaders().get("X-Request-ID"));
        assertFalse(modifiedRequest.getHeaders().getOrEmpty("X-Request-ID").isEmpty());
    }

    @Test
    void filter_shouldRecordStartTime() {
        // Act
        loggingFilter.filter(exchange, filterChain);

        // Assert
        Object startTime = exchange.getAttribute("startTime");
        assertNotNull(startTime);
        assertTrue(startTime instanceof Long);
    }

    @Test
    void filter_shouldLogRequestDetails() {
        // This test is limited since we can't easily verify log output in a unit test
        // In a real scenario, we might use a log appender to capture log output

        // Act - should not throw any exceptions
        Mono<Void> result = loggingFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void filter_shouldLogResponseDetails() {
        // Setup the response with a status code
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);

        // Act
        Mono<Void> result = loggingFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getOrder_shouldReturnNegativeOne() {
        // Act
        int order = loggingFilter.getOrder();

        // Assert
        assertEquals(-1, order);
    }
}
