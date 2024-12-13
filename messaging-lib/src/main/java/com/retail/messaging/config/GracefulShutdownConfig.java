package com.retail.messaging.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

/**
 * @author Angel Zlatenov
 */

@Configuration
public class GracefulShutdownConfig {
    private final GracefulShutdownHandler shutdownHandler;

    public GracefulShutdownConfig(GracefulShutdownHandler shutdownHandler) {
        this.shutdownHandler = shutdownHandler;
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(shutdownHandler);
        return factory;
    }
}
