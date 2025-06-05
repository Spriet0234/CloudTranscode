package com.mediatranscode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, name = "file_name")
    private String fileName;
    
    @Column(nullable = false, name = "original_file_name")
    private String originalFileName;
    
    @Column(nullable = false)
    private String mimeType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.QUEUED;
    
    @Column
    private Integer progress = 0;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column
    private Long processedSize;
    
    @Column
    private Integer duration;
    
    @Column
    private Double compressionRatio;
    
    @Column
    private Long processingTime;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @Column
    private String downloadUrl;
    
    @Column
    private String s3Key;
    
    @Column
    private String processedS3Key;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    // Processing settings stored as JSON
    @Column(columnDefinition = "TEXT")
    private String settingsJson;
    
    public enum JobStatus {
        QUEUED, PROCESSING, COMPLETED, FAILED
    }
} 