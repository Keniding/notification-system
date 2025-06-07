package com.keniding.notificationSystem.producer;

import com.keniding.notificationSystem.config.KafkaConfig;
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
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing user registered event for user: {}", event.getUserId());

        CompletableFuture<SendResult<String, UserRegisteredEvent>> future = kafkaTemplate.send(KafkaConfig.USER_REGISTERED_TOPIC, event.getUserId(), event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Successfully sent user registered event for user: {} with offset: {}", event.getUserId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send user registered event for user: {}", event.getUserId(), exception);
            }
        });
    }

    public void publishUserRegisteredSync(UserRegisteredEvent event) {
        try {
            log.info("Publishing user registered event synchronously for user: {}", event.getUserId());

            SendResult<String, UserRegisteredEvent> result = kafkaTemplate.send(KafkaConfig.USER_REGISTERED_TOPIC, event.getUserId(), event).get();

            log.info("Successfully sent user registered event synchronously for user: {} with offset: {}", event.getUserId(),result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to send user registered event for user: {}", event.getUserId(), e);
            throw new RuntimeException("Failed to publish user registered event", e);
        }
    }
}
