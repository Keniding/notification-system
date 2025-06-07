package com.keniding.notificationSystem.consumer;

import com.keniding.notificationSystem.config.KafkaConfig;
import com.keniding.notificationSystem.dto.AnalyticsStats;
import com.keniding.notificationSystem.dto.UserAnalyticsEvent;
import com.keniding.notificationSystem.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class AnalyticsConsumer {

    private final AtomicLong totalRegistrations = new AtomicLong(0);
    private final AtomicLong totalEvents = new AtomicLong(0);

    @KafkaListener(
            topics = KafkaConfig.USER_REGISTERED_TOPIC,
            groupId = "analytics-group",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("Processing user registered event for analytics - User: {}, Partition: {}, Offset: {}",
                    event.getUserId(), partition, offset);
            procesRegistrationAnalytics(event);
            acknowledgment.acknowledge();

            if (totalRegistrations.get() % 5 == 0) {
                printCurrentStats();
            }
        } catch (Exception e) {
            log.error("Error processing analytics for user: {}", event.getUserId(), e);
            acknowledgment.acknowledge(); // Acknowledge even on error to avoid infinite retry
        }
    }

    @KafkaListener(
            topics = KafkaConfig.USER_ANALYTICS_TOPIC,
            groupId = "analytics-events-group",
            containerFactory = "userAnalyticsKafkaListenerContainerFactory"
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
        long count = totalRegistrations.incrementAndGet();
        log.info("User registration analytics processed. Total registrations: {}", count);
    }

    private void processSpecificAnalytics(UserAnalyticsEvent event) {
        long count = totalEvents.incrementAndGet();
        log.debug("Analytics event processed: {}. Total events: {}", event.getEvent(), count);
    }

    private void printCurrentStats() {
        log.info("=== ANALYTICS STATS ===");
        log.info("Total Registrations: {}", totalRegistrations.get());
        log.info("Total Events: {}", totalEvents.get());
        log.info("Timestamp: {}", LocalDateTime.now());
        log.info("=======================");
    }

    public AnalyticsStats getCurrentStats() {
        return AnalyticsStats.builder()
                .totalRegistrations(totalRegistrations.get())
                .totalEvents(totalEvents.get())
                .timestamp(LocalDateTime.now())
                .status("active")
                .build();
    }

    public String getCurrentStatsAsString() {
        AnalyticsStats stats = getCurrentStats();
        return String.format(
                "=== ANALYTICS STATS ===\n" +
                        "Total Registrations: %d\n" +
                        "Total Events: %d\n" +
                        "Timestamp: %s\n" +
                        "Status: %s\n" +
                        "=======================",
                stats.getTotalRegistrations(),
                stats.getTotalEvents(),
                stats.getTimestamp(),
                stats.getStatus()
        );
    }

    public long getTotalRegistrations() {
        return totalRegistrations.get();
    }

    public long getTotalEvents() {
        return totalEvents.get();
    }
}
