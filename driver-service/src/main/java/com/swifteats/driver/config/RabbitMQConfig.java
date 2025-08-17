package com.swifteats.driver.config;

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
    public static final String DRIVER_ASSIGNMENT_QUEUE = "driver.assignment";
    public static final String DRIVER_LOCATION_QUEUE = "driver.location";
    public static final String ORDER_STATUS_UPDATE_QUEUE = "order.status.update";

    // Exchange names
    public static final String DRIVER_EXCHANGE = "driver.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";

    // Routing keys
    public static final String DRIVER_ASSIGNMENT_KEY = "driver.assignment";
    public static final String DRIVER_LOCATION_KEY = "driver.location";
    public static final String ORDER_STATUS_UPDATE_KEY = "order.status.update";

    @Bean
    public Queue driverAssignmentQueue() {
        return new Queue(DRIVER_ASSIGNMENT_QUEUE, true);
    }

    @Bean
    public Queue driverLocationQueue() {
        return new Queue(DRIVER_LOCATION_QUEUE, true);
    }

    @Bean
    public Queue orderStatusUpdateQueue() {
        return new Queue(ORDER_STATUS_UPDATE_QUEUE, true);
    }

    @Bean
    public DirectExchange driverExchange() {
        return new DirectExchange(DRIVER_EXCHANGE);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding driverAssignmentBinding() {
        return BindingBuilder
                .bind(driverAssignmentQueue())
                .to(driverExchange())
                .with(DRIVER_ASSIGNMENT_KEY);
    }

    @Bean
    public Binding driverLocationBinding() {
        return BindingBuilder
                .bind(driverLocationQueue())
                .to(driverExchange())
                .with(DRIVER_LOCATION_KEY);
    }

    @Bean
    public Binding orderStatusUpdateBinding() {
        return BindingBuilder
                .bind(orderStatusUpdateQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_UPDATE_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
