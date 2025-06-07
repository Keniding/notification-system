package com.keniding.notificationSystem.producer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.UserAnalyticsEvent;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing user registered event for user: {}", event.getUserId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.USER_REGISTERED_TOPIC,
                event.getUserId(),
                event
        );

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully sent user registered event for user: {} with offset: {}",
                        event.getUserId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user registered event for user: {}", event.getUserId(), exception);
            }
        });
    }

    public void publishUserRegisteredSync(UserRegisteredEvent event) {
        try {
            log.info("Publishing user registered event synchronously for user: {}", event.getUserId());

            SendResult<String, Object> result = kafkaTemplate.send(
                    KafkaConfig.USER_REGISTERED_TOPIC,
                    event.getUserId(),
                    event
            ).get();

            log.info("Successfully sent user registered event synchronously for user: {} with offset: {}",
                    event.getUserId(), result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to send user registered event for user: {}", event.getUserId(), e);
            throw new RuntimeException("Failed to publish user registered event", e);
        }
    }

    // Nuevo método para publicar eventos de analytics
    public void publishAnalyticsEvent(UserAnalyticsEvent event) {
        log.debug("Publishing analytics event: {} for user: {}", event.getEvent(), event.getUserId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.USER_ANALYTICS_TOPIC,
                event.getUserId(),
                event
        );

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.debug("Successfully sent analytics event: {} for user: {} with offset: {}",
                        event.getEvent(), event.getUserId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send analytics event: {} for user: {}",
                        event.getEvent(), event.getUserId(), exception);
            }
        });
    }

    // Método para publicar a SMS topic
    public void publishSmsNotification(UserRegisteredEvent event) {
        log.info("Publishing SMS notification event for user: {}", event.getUserId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.SMS_NOTIFICATIONS_TOPIC,
                event.getUserId(),
                event
        );

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully sent SMS notification event for user: {} with offset: {}",
                        event.getUserId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send SMS notification event for user: {}", event.getUserId(), exception);
            }
        });
    }
}
