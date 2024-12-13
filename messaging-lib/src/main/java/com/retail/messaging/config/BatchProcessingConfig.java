package com.retail.messaging.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchProcessingConfig {

    @Value("${messaging.consumer.batch.size:100}")
    private int batchSize;

    @Value("${messaging.consumer.batch.timeout:5000}")
    private long batchTimeout;

    @Bean
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true);
        factory.setBatchSize(batchSize);
        factory.setReceiveTimeout(batchTimeout);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(batchSize * 2);
        return factory;
    }
}