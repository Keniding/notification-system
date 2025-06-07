package com.keniding.notificationSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsNotificationEvent {
    private String userId;
    private String phone;
    private String message;
    private String country;
    private LocalDateTime createdAt;
}
