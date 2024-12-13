package com.retail.messaging.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.ssl.enabled:true}")
    private boolean sslEnabled;

    @Bean
    public ConnectionFactory connectionFactory() throws NoSuchAlgorithmException, KeyManagementException {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        if (sslEnabled) {
            connectionFactory.getRabbitConnectionFactory().useSslProtocol();
        }

        // Enable publisher confirms and returns
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        // Configure mandatory publishing
        rabbitTemplate.setMandatory(true);

        // Configure confirmation callback
        rabbitTemplate.setConfirmCallback((correlation, ack, reason) -> {
            if (!ack) {
                // Handle nack
                // We'll implement this in the RetryableMessagePublisher
            }
        });

        // Configure return callback for unroutable messages
        rabbitTemplate.setReturnsCallback(returned -> {
            // Handle returned message
            // We'll implement this in the RetryableMessagePublisher
        });

        return rabbitTemplate;
    }
}