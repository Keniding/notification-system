package com.keniding.notificationSystem.consumer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.UserAnalyticsEvent;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class AnalyticsConsumer {
    private final ConcurrentHashMap<String, AtomicLong> concurrentStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> sourceStats = new ConcurrentHashMap<>();
    private final AtomicLong totalRegistrations =  new AtomicLong(0);

    @KafkaListener(
            topics = KafkaConfig.USER_REGISTERED_TOPIC,
            groupId = "analytics-group"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
            ) {
        try {
            log.info("Processing user registered event for analytics - User: {}, Partition: {}, Offset: {}", event, partition, offset);
            procesRegistrationAnalytics(event);
            acknowledgment.acknowledge();

            if (totalRegistrations.get() % 5 == 0) {
                printCurrentStats();
            }
        } catch (Exception e) {
            log.error("Error processing analytics for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = KafkaConfig.USER_ANALYTICS_TOPIC,
            groupId = "analytics-events-group"
    )
    public void handleAnalyticsEvent(
             @Payload UserAnalyticsEvent event,
             Acknowledgment acknowledgment) {
        try {
            log.debug("Processing analytics event: {} for user: {}", event.getEvent(), event.getUserId());
            processSpecificAnalytics(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing analytics event for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge();
        }
    }

    private void procesRegistrationAnalytics(UserRegisteredEvent event) {
        long total = totalRegistrations.incrementAndGet();

        if (event.getCountry() != null) {
            concurrentStats.computeIfAbsent(event.getCountry(), k -> new AtomicLong(0)).incrementAndGet();
        }

        log.info("Registration analytics update - Total: {}, Country: {}, Source: {}", total, event.getCountry(), event.getSource());
    }

    private void processSpecificAnalytics(UserAnalyticsEvent event) {
        switch (event.getEvent()) {
            case "EMAIL_SENT":
                break;
            case "SMS_SENT":
                break;
            default:
                log.debug("Unknown event type: {}", event.getEvent());
        }
    }

    private void printCurrentStats() {
        log.info("=== CURRENT ANALYTICS STATS ===");
        log.info("Total registrations: {}", totalRegistrations.get());
        log.info("Registrations by Country:");
        concurrentStats.forEach((source, count) -> log.info("\t{}: {}", source, count.get()));
        log.info("===============================");
    }

    public String getCurrentStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Total Registrations: ")
                .append(totalRegistrations.get()).append("\n");
        stats.append("By Country: ")
                .append(concurrentStats).append("\n");
        stats.append("By Source: ")
                .append(sourceStats).append("\n");
        return stats.toString();
    }
}
