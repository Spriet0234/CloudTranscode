package com.mediatranscode.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private String id;
    private String filename;
    private String originalFilename;
    private String mimeType;
    private String status;
    private Double progress;
    private Long fileSize;
    private Long processedSize;
    private Double compressionRatio;
    private Integer processingTime;
    private String errorMessage;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
} 