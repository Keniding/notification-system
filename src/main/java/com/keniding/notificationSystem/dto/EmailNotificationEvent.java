package com.keniding.notificationSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailNotificationEvent {
    private String userId;
    private String email;
    private String name;
    private String emailType; // WELCOME, CONFIRMATION
    private LocalDateTime createdAt;
}
