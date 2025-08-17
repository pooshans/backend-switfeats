package com.swifteats.order.messaging;

import com.swifteats.order.dto.OrderDTO;
import com.swifteats.order.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishNewOrder(OrderDTO orderDTO) {
        log.info("Publishing new order to queue: {}", orderDTO.getId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                orderDTO);
    }

    public void publishOrderStatusUpdate(OrderDTO orderDTO) {
        log.info("Publishing order status update to queue: {} - {}", orderDTO.getId(), orderDTO.getStatus());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_STATUS_ROUTING_KEY,
                orderDTO);
    }

    public void publishDriverAssignment(OrderDTO orderDTO) {
        log.info("Publishing driver assignment to queue: {} - Driver: {}",
                orderDTO.getId(), orderDTO.getDriverId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.DRIVER_ASSIGNMENT_ROUTING_KEY,
                orderDTO);
    }
}
