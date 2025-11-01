package com.trackwize.cloud.notification.activemq.base;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;

@Slf4j
public abstract class JmsListenerBase {

    public final void handle(Message message) throws JMSException {
        Instant start = Instant.now();
        String messageId = getMessageId(message);
        String destination = getDestination(message);

        MDC.put("messageContext", "[messageId=%s, destination=%s]".formatted(messageId, destination));

        try {
            onMessage(message);
        } catch (Exception e) {
            log.error("Error while processing JMS message", e);
        } finally {
            log.info("Processed JMS message in {} ms",
                    Duration.between(start, Instant.now()).toMillis());
            MDC.clear();
        }
    }

    protected abstract void onMessage(Message message) throws JMSException;

    private String getMessageId(Message message) {
        try {
            return StringUtils.defaultIfBlank(message.getJMSMessageID(), "N/A");
        } catch (Exception e) {
            log.warn("Failed to read JMSMessageID", e);
            return "N/A";
        }
    }

    private String getDestination(Message message) {
        try {
            return message.getJMSDestination() != null
                    ? message.getJMSDestination().toString()
                    : "UNKNOWN";
        } catch (Exception e) {
            log.warn("Failed to read JMSDestination", e);
            return "UNKNOWN";
        }
    }
}
