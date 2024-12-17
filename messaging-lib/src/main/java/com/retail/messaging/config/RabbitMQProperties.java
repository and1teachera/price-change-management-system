package com.retail.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import lombok.Data;

/**
 * @author Angel Zlatenov
 */

@ConfigurationProperties(prefix = "rabbitmq")
@Data
public class RabbitMQProperties {
    private final Exchange exchange;
    private final Queue queue;

    @ConstructorBinding
    public RabbitMQProperties(Exchange exchange, Queue queue) {
        this.exchange = exchange;
        this.queue = queue;
    }

    @Data
    public static class Exchange {
        private String pas;
        private String pad;
        private String dlx;
    }

    @Data
    public static class Queue {
        private String pas;
        private String pad;
    }
}
