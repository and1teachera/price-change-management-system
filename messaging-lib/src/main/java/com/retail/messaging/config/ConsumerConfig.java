package com.retail.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/**
 * @author Angel Zlatenov
 */

@ConfigurationProperties(prefix = "messaging.consumer")
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

    public int getBatchSize() {
        return batchSize;
    }

    public int getConcurrentProcessors() {
        return concurrentProcessors;
    }

    public long getBatchTimeout() {
        return batchTimeout;
    }
}