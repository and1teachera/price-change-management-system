package com.retail.messaging.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import com.rabbitmq.client.Connection;

/**
 * @author Angel Zlatenov
 */


@Component
public class RabbitHealthIndicator extends AbstractHealthIndicator {
    private final ConnectionFactory connectionFactory;

    public RabbitHealthIndicator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            Connection connection = connectionFactory.createConnection().getDelegate();
            builder.up()
                    .withDetail("connected", connection.isOpen())
                    .withDetail("queueCount", getQueueCount(connection));
        } catch (Exception e) {
            builder.down(e);
        }
    }

    private int getQueueCount(Connection connection) {
        try {
            return connection.getServerProperties().size();
        } catch (Exception e) {
            return -1;
        }
    }
}