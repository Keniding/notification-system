package com.keniding.notificationSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsStats {
    private long totalRegistrations;
    private long totalEvents;
    private LocalDateTime timestamp;
    private String status;
}
