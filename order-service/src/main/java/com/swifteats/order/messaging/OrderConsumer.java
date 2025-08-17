package com.swifteats.order.messaging;

import com.swifteats.order.config.RabbitMQConfig;
import com.swifteats.order.dto.OrderDTO;
import com.swifteats.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void consumeNewOrder(OrderDTO orderDTO) {
        log.info("Consumed new order from queue: {}", orderDTO.getId());
        try {
            // Process the order
            orderService.processOrder(orderDTO);
        } catch (Exception e) {
            log.error("Error processing order from queue", e);
            // In a production system, you might want to:
            // 1. Implement retry logic
            // 2. Move to a dead letter queue
            // 3. Send alerts
            // For now, we're just logging the error
        }
    }

    // Additional consumers can be added as needed
}
