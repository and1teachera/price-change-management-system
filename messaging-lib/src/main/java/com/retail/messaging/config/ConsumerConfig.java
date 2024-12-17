package com.retail.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import lombok.Data;

/**
 * @author Angel Zlatenov
 */

@ConfigurationProperties(prefix = "messaging.consumer")
@Data
public class ConsumerConfig {
    private final int batchSize;
    private final int concurrentProcessors;
    private final long batchTimeout;

    @ConstructorBinding
    public ConsumerConfig(int batchSize, int concurrentProcessors, long batchTimeout) {
        this.batchSize = batchSize;
        this.concurrentProcessors = concurrentProcessors;
        this.batchTimeout = batchTimeout;
    }
}
