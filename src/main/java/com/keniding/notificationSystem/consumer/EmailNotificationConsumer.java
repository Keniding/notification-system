package com.keniding.notificationSystem.consumer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.UserAnalyticsEvent;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import com.keniding.notificationSystem.producer.UserEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final UserEventProducer userEventProducer;

    @KafkaListener(
            topics = KafkaConfig.USER_REGISTERED_TOPIC,
            groupId = "email-notification-group",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing user registered event for email notification - User: {}, Topic: {}, Partition: {}, offset: {}",
                    event.getUserId(), topic, partition, offset);
            processWelcomeEmail(event);
            publishEmailEvent(event);
            publishAnalyticsEvent(event, "EMAIL_SENT");
            acknowledgment.acknowledge();

            log.info("Successfully processed email notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error while processing email notification for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge();
        }
    }

    private void processWelcomeEmail(UserRegisteredEvent event) {
        log.info("Sending welcome email to user: {} at email: {}", event.getUserId(), event.getEmail());
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Welcome email sent successfully to user: {}", event.getUserId());
    }

    private void publishEmailEvent(UserRegisteredEvent event) {
        // This would typically publish to an email-specific topic
        log.debug("Publishing email event for user: {}", event.getUserId());
    }

    private void publishAnalyticsEvent(UserRegisteredEvent event, String eventType) {
        UserAnalyticsEvent analyticsEvent = UserAnalyticsEvent.builder()
                .userId(event.getUserId())
                .event(eventType)
                .source("email-service")
                .country(event.getCountry())
                .timestamp(LocalDateTime.now())
                .build();

        userEventProducer.publishAnalyticsEvent(analyticsEvent);
    }
}
