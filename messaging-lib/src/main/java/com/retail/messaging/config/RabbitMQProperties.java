package com.retail.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
/**
 * @author Angel Zlatenov
 */


@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    private final Exchange exchange;
    private final Queue queue;

    @ConstructorBinding
    public RabbitMQProperties(Exchange exchange, Queue queue) {
        this.exchange = exchange;
        this.queue = queue;
    }

    public static class Exchange {
        private String pas;
        private String pad;
        private String dlx;

        public String getPas() { return pas; }
        public void setPas(String pas) { this.pas = pas; }
        public String getPad() { return pad; }
        public void setPad(String pad) { this.pad = pad; }
        public String getDlx() { return dlx; }
        public void setDlx(String dlx) { this.dlx = dlx; }
    }

    public static class Queue {
        private String pas;
        private String pad;

        public String getPas() { return pas; }
        public void setPas(String pas) { this.pas = pas; }
        public String getPad() { return pad; }
        public void setPad(String pad) { this.pad = pad; }
    }

    public Exchange getExchange() { return exchange; }
    public Queue getQueue() { return queue; }
}