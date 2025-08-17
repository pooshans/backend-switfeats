package com.swifteats.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String ORDER_QUEUE = "order-queue";
    public static final String ORDER_STATUS_QUEUE = "order-status-queue";
    public static final String DRIVER_ASSIGNMENT_QUEUE = "driver-assignment-queue";

    // Exchange names
    public static final String ORDER_EXCHANGE = "order-exchange";

    // Routing keys
    public static final String ORDER_ROUTING_KEY = "order.new";
    public static final String ORDER_STATUS_ROUTING_KEY = "order.status";
    public static final String DRIVER_ASSIGNMENT_ROUTING_KEY = "order.driver";

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORDER_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ORDER_STATUS_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue driverAssignmentQueue() {
        return QueueBuilder.durable(DRIVER_ASSIGNMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DRIVER_ASSIGNMENT_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue orderDlq() {
        return QueueBuilder.durable(ORDER_QUEUE + ".dlq").build();
    }

    @Bean
    public Queue orderStatusDlq() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE + ".dlq").build();
    }

    @Bean
    public Queue driverAssignmentDlq() {
        return QueueBuilder.durable(DRIVER_ASSIGNMENT_QUEUE + ".dlq").build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder.bind(orderStatusQueue()).to(orderExchange()).with(ORDER_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding driverAssignmentBinding() {
        return BindingBuilder.bind(driverAssignmentQueue()).to(orderExchange()).with(DRIVER_ASSIGNMENT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
