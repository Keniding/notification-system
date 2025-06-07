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
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final UserEventProducer userEventProducer;
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of("US", "CA", "GB", "ES", "FR", "DE");

    @KafkaListener(
            topics = KafkaConfig.SMS_NOTIFICATIONS_TOPIC,
            groupId = "sms-notification-group",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void handlerUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partitions,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing user registered event for SMS notification - User: {}, Partition: {}, Offset: {}",
                    event.getUserId(), partitions, offset);

            if (!isSmsSupported(event.getCountry())) {
                log.warn("SMS not supported for country: {}, Partition: {}, Offset: {}",
                        event.getCountry(), partitions, offset);
                acknowledgment.acknowledge();
                return;
            }

            String message = processSmsNotification(event);
            publishSmsEvent(event, message);
            publishAnalyticsEvent(event, "SMS_SENT");
            acknowledgment.acknowledge();

            log.info("Successfully processed SMS notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error while processing SMS notification for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge();
        }
    }

    private boolean isSmsSupported(String country) {
        return SUPPORTED_COUNTRIES.contains(country);
    }

    private String processSmsNotification(UserRegisteredEvent event) {
        String message = String.format("Welcome %s! Your account has been created successfully. Country: %s",
                event.getUserId(), event.getCountry());
        log.info("Sending SMS to user: {} with message: {}", event.getUserId(), message);

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("SMS sent successfully to user: {}", event.getUserId());
        return message;
    }

    private void publishSmsEvent(UserRegisteredEvent event, String message) {
        log.debug("Publishing SMS event for user: {} with message length: {}",
                event.getUserId(), message.length());
    }

    private void publishAnalyticsEvent(UserRegisteredEvent event, String eventType) {
        UserAnalyticsEvent analyticsEvent = UserAnalyticsEvent.builder()
                .userId(event.getUserId())
                .event(eventType)
                .source("sms-service")
                .country(event.getCountry())
                .timestamp(LocalDateTime.now())
                .build();

        userEventProducer.publishAnalyticsEvent(analyticsEvent);
    }
}
