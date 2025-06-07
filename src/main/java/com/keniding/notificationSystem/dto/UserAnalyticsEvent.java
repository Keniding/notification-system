package com.keniding.notificationSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsEvent {
    private String userId;
    private String event; // USER_REGISTERED, EMAIL_SENT, SMS_SENT
    private String source;
    private String country;
    private LocalDateTime timestamp;
}
