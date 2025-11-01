package com.trackwize.cloud.notification.activemq;

import com.trackwize.cloud.notification.activemq.base.JmsListenerBase;
import com.trackwize.cloud.notification.service.NotificationService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveMQReceiver extends JmsListenerBase {

    private final NotificationService notificationService;

    @JmsListener(destination = "${app.queue.email}", containerFactory = "jmsListenerContainerFactory")
    public void handleEmail(TextMessage message) throws Exception {
        log.info("[EmailQueue] Received: {}", message.getText());
        notificationService.processQueueMessage(message);
    }

    @JmsListener(destination = "${app.queue.inbox}", containerFactory = "jmsListenerContainerFactory")
    public void handleInbox(TextMessage message) throws Exception {
        log.info("[InboxQueue] Received: {}", message.getText());
        notificationService.processQueueMessage(message);
    }

    @Override
    protected void onMessage(Message message) throws JMSException {
    }
}
