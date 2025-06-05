package com.mediatranscode.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobResponse {
    private String id;
    private String fileName;
    private String status;
    private Integer progress;
    private Long fileSize;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
} 