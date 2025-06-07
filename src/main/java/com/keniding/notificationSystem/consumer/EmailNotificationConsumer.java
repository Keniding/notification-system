package com.keniding.notificationSystem.consumer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.EmailNotificationEvent;
import com.keniding.notificationSystem.dto.UserAnalyticsEvent;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationConsumer {
    private final KafkaTemplate<String, EmailNotificationEvent> emailKafkaTemplate;
    private final KafkaTemplate<String, UserAnalyticsEvent> analyticsTemplate;
    
    @KafkaListener(
            topics = KafkaConfig.USER_REGISTERED_TOPIC,
            groupId = "email-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing user registered event for email notification - User: {}, Topic: {}, Partition: {}, offset: {}", event.getUserId(), topic, partition, offset);
            processWelcomeEmail(event);
            publishEmailEvent(event);
            publishAnalyticsEvent(event, "EMAIL_SENT");
            acknowledgment.acknowledge();
            
            log.info("Successfully processed email notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error while processing email notification for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge(); // Para evitar processing infinito
        }
    }
    
    private void processWelcomeEmail(UserRegisteredEvent event) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
            log.info("Sending welcome email to: {} ({})", event.getName(), event.getEmail());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw  new RuntimeException("Email processing interrupted", e);
        }
    }
    
    private void publishEmailEvent(UserRegisteredEvent event) {
        EmailNotificationEvent emailEvent = new EmailNotificationEvent(
                event.getUserId(),
                event.getEmail(),
                event.getName(),
                "Welcome",
                LocalDateTime.now()
        );
        
        emailKafkaTemplate.send(KafkaConfig.EMAIL_NOTIFICATIONS_TOPIC, emailEvent.getUserId(), emailEvent);
        log.info("Published email notification event for user: {}", event.getUserId());
    }
    
    private void publishAnalyticsEvent(UserRegisteredEvent event, String eventType) {
        UserAnalyticsEvent analyticsEvent = new UserAnalyticsEvent(
                event.getUserId(), 
                eventType,
                event.getSource(),
                event.getCountry(),
                LocalDateTime.now()
        );
        
        analyticsTemplate.send(KafkaConfig.USER_ANALYTICS_TOPIC, analyticsEvent.getUserId(), analyticsEvent);
        log.debug("Published analytics event: {} for user: {}", eventType, event.getUserId());
    }
}
