package com.retail.messaging.config;


import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Angel Zlatenov
 */

@Component
public class GracefulShutdownHandler implements TomcatConnectorCustomizer,
                                                ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownHandler.class);
    private volatile Connector connector;
    private static final int SHUTDOWN_TIMEOUT = 30;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (this.connector == null) {
            return;
        }

        logger.info("Starting graceful shutdown of RabbitMQ message processing");
        try {
            pauseAndShutdown();
        } catch (Exception e) {
            logger.error("Error during graceful shutdown", e);
        }
    }

    private void pauseAndShutdown() {
        this.connector.pause();

        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            try {
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    logger.warn("Thread pool did not terminate within {} seconds. " +
                            "Proceeding with forceful shutdown", SHUTDOWN_TIMEOUT);
                    threadPoolExecutor.shutdownNow();

                    if (!threadPoolExecutor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                        logger.error("Thread pool did not terminate even after forceful shutdown");
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.warn("Shutdown process interrupted. Proceeding with forceful shutdown");
                threadPoolExecutor.shutdownNow();
            }
        }
    }
}
