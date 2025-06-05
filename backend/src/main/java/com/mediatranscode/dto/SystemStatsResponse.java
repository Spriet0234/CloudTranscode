package com.mediatranscode.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemStatsResponse {
    private Long totalFilesProcessed;
    private Long activeUsers;
    private Long totalProcessingTime;
    private Long storageUsed;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double storageUsage;
    private Double networkIO;
    private Long filesInQueue;
    private Double avgProcessingTime;
    private Double successRate;
    private Long uptime;
    private List<ActivityResponse> recentActivity;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityResponse {
        private String type;
        private String message;
        private String time;
        private String status;
    }
} 