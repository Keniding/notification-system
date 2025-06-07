package com.keniding.notificationSystem.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String USER_REGISTERED_TOPIC = "user-registered";
    public static final String EMAIL_NOTIFICATIONS_TOPIC = "email-notifications";
    public static final String SMS_NOTIFICATIONS_TOPIC = "sms-notifications";
    public static final String USER_ANALYTICS_TOPIC = "user-analytics";

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailNotificationsTopic() {
        return TopicBuilder.name(EMAIL_NOTIFICATIONS_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic smsNotificationsTopic() {
        return TopicBuilder.name(SMS_NOTIFICATIONS_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userAnalyticsTopic() {
        return TopicBuilder.name(USER_ANALYTICS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
