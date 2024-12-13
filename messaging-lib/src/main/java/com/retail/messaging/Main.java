package com.retail.messaging;

import com.retail.messaging.config.ConsumerConfig;
import com.retail.messaging.config.RabbitMQProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Angel Zlatenov
 */

@EnableConfigurationProperties({ConsumerConfig.class, RabbitMQProperties.class})
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}