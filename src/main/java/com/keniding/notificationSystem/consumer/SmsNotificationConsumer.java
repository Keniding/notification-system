package com.keniding.notificationSystem.consumer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.SmsNotificationEvent;
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
public class SmsNotificationConsumer {
    private final KafkaTemplate<String, SmsNotificationEvent> smsKafkaTemplate;
    private final KafkaTemplate<String, UserAnalyticsEvent>  analyticsKafkaTemplate;

    @KafkaListener(
            topics = KafkaConfig.SMS_NOTIFICATIONS_TOPIC,
            groupId = "sms-notification-group"
    )
    public void handlerUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partitions,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment
            ) {
        try {
            log.info("Processing user registered event for SMS notification - User: {}, Partition: {}, Offset: {}", event, partitions, offset);

            if (!isSmsSupported(event.getCountry())) {
                log.warn("SMS not supported for country: {}, Partition: {}, Offset: {}", event.getCountry(), partitions, offset);
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
        return country != null && !country.equals("RESTRICTED");
    }

    private String processSmsNotification(UserRegisteredEvent event) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 800));
            String message = String.format("Welcome %s! Your account has successfully create. Thanks for registered", event.getName());
            log.info("Sending SMS to: {} ({}): {}", event.getName(), event.getPhone(), message);

            return message;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS notification processing failed", e);
        }
    }

    private void publishSmsEvent(UserRegisteredEvent event, String message) {
        SmsNotificationEvent smsEvent = new SmsNotificationEvent(
                event.getUserId(),
                event.getPhone(),
                message,
                event.getCountry(),
                LocalDateTime.now()
        );

        smsKafkaTemplate.send(KafkaConfig.SMS_NOTIFICATIONS_TOPIC, smsEvent.getUserId(), smsEvent);
        log.debug("Published SMS notification to topic: {}", smsEvent.getUserId());
    }

    private void publishAnalyticsEvent(UserRegisteredEvent event, String eventType) {
        UserAnalyticsEvent analyticsEvent = new UserAnalyticsEvent(
                event.getUserId(),
                eventType,
                event.getSource(),
                event.getCountry(),
                LocalDateTime.now()
        );

        analyticsKafkaTemplate.send(KafkaConfig.USER_ANALYTICS_TOPIC, analyticsEvent.getUserId(), analyticsEvent);
        log.debug("Published UserAnalytics event to topic: {}", analyticsEvent.getUserId());
    }
}
