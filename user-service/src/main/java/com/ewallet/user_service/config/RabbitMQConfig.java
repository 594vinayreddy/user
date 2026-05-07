package com.ewallet.user_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ewallet.exchange";

    // user-service consumes from this queue (registration events from auth-service)
    public static final String USER_QUEUE             = "user.registered.queue";
    // user-service publishes to wallet-service on account deletion
    public static final String USER_TO_WALLET_QUEUE   = "user_to_wallet.registered.queue";
    // user-service publishes to auth-service on account deletion
    public static final String USER_TO_AUTH_QUEUE     = "user_to_auth.registered.queue";

    public static final String USER_ROUTING_KEY           = "user.registered";
    public static final String USER_TO_WALLET_ROUTING_KEY = "user_to_wallet.registered";
    public static final String USER_TO_AUTH_ROUTING_KEY   = "user_to_auth.registered";

    @Bean
    public DirectExchange ewalletExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // --- Queues ---

    // User-service consumes registration events published by auth-service
    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(USER_QUEUE).build();
    }

    // User-service publishes account deletion events to wallet-service
    @Bean
    public Queue userToWalletQueue() {
        return QueueBuilder.durable(USER_TO_WALLET_QUEUE).build();
    }

    // User-service publishes account deletion events to auth-service
    @Bean
    public Queue userToAuthQueue() {
        return QueueBuilder.durable(USER_TO_AUTH_QUEUE).build();
    }

    // --- Bindings ---

    @Bean
    public Binding userBinding(Queue userQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(userQueue)
                .to(ewalletExchange)
                .with(USER_ROUTING_KEY);
    }

    @Bean
    public Binding userToWalletBinding(Queue userToWalletQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(userToWalletQueue)
                .to(ewalletExchange)
                .with(USER_TO_WALLET_ROUTING_KEY);
    }

    @Bean
    public Binding userToAuthBinding(Queue userToAuthQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(userToAuthQueue)
                .to(ewalletExchange)
                .with(USER_TO_AUTH_ROUTING_KEY);
    }

    // --- Messaging Infrastructure ---

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Needed to publish account deletion events to auth and wallet services
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Needed to consume from user.registered.queue
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}