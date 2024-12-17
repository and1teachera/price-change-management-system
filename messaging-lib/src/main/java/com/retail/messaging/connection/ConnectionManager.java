package com.retail.messaging.connection;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.Data;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Data
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private final ConnectionFactory connectionFactory;
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Connection> connections;
    private final ExecutorService connectionExecutor;
    private final AtomicBoolean shutdownInitiated;
    private static final int RECOVERY_INTERVAL_MS = 5000;

    @Autowired
    public ConnectionManager(ConnectionFactory connectionFactory, MeterRegistry meterRegistry) {
        this.connectionFactory = connectionFactory;
        this.meterRegistry = meterRegistry;
        this.connections = new ConcurrentHashMap<>();
        this.connectionExecutor = Executors.newCachedThreadPool();
        this.shutdownInitiated = new AtomicBoolean(false);

        setupMetrics();
        startConnectionMonitoring();
    }

    public Connection getConnection(String connectionId) throws IOException, TimeoutException {
        if (shutdownInitiated.get()) {
            throw new IllegalStateException("Connection manager is shutting down");
        }

        return connections.computeIfAbsent(connectionId, id -> {
            try {
                Connection connection = createConnection();
                setupConnectionRecovery(id, connection);
                return connection;
            } catch (IOException | TimeoutException e) {
                logger.error("Failed to create connection: {}", id, e);
                throw new RuntimeException("Connection creation failed", e);
            }
        });
    }

    private Connection createConnection() throws IOException, TimeoutException {
        Connection connection = connectionFactory.newConnection(connectionExecutor);
        meterRegistry.counter("rabbitmq.connections.created").increment();
        return connection;
    }

    private void setupConnectionRecovery(String connectionId, Connection connection) {
        connection.addShutdownListener(cause -> {
            if (!shutdownInitiated.get()) {
                logger.warn("Connection lost: {}. Initiating recovery...", connectionId);
                meterRegistry.counter("rabbitmq.connections.lost").increment();

                connectionExecutor.submit(() -> {
                    while (!shutdownInitiated.get()) {
                        try {
                            Thread.sleep(RECOVERY_INTERVAL_MS);
                            Connection newConnection = createConnection();
                            connections.put(connectionId, newConnection);
                            setupConnectionRecovery(connectionId, newConnection);
                            logger.info("Connection recovered: {}", connectionId);
                            meterRegistry.counter("rabbitmq.connections.recovered").increment();
                            break;
                        } catch (Exception e) {
                            logger.error("Recovery attempt failed for connection: {}", connectionId, e);
                        }
                    }
                });
            }
        });
    }

    private void setupMetrics() {
        meterRegistry.gauge("rabbitmq.connections.active", connections, ConcurrentHashMap::size);
    }

    private void startConnectionMonitoring() {
        connectionExecutor.submit(() -> {
            while (!shutdownInitiated.get()) {
                try {
                    Thread.sleep(30000); // Monitor every 30 seconds
                    connections.forEach((id, connection) -> {
                        if (!connection.isOpen() && !shutdownInitiated.get()) {
                            logger.warn("Detected closed connection: {}", id);
                            connections.remove(id);
                            meterRegistry.counter("rabbitmq.connections.closed").increment();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        shutdownInitiated.set(true);
        logger.info("Initiating connection manager shutdown");

        connections.forEach((id, connection) -> {
            try {
                connection.close();
                logger.info("Closed connection: {}", id);
            } catch (IOException e) {
                logger.error("Error closing connection: {}", id, e);
            }
        });

        connectionExecutor.shutdown();
        logger.info("Connection manager shutdown complete");
    }
}
