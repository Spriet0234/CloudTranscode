package com.mediatranscode.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private String originalFilename;
    
    @Column(nullable = false)
    private String mimeType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.QUEUED;
    
    @Column(nullable = false)
    private Double progress = 0.0;
    
    @Column(nullable = false)
    private Integer priority = 1;
    
    // Processing settings stored as JSON
    @Column(columnDefinition = "TEXT")
    private String settings;
    
    // File paths
    private String originalPath;
    private String processedPath;
    private String thumbnailPath;
    
    // Processing results
    private Long processedSize;
    private Double compressionRatio;
    private Integer processingTime; // in milliseconds
    
    // Error handling
    private String errorMessage;
    private Integer retryCount = 0;
    private Integer maxRetries = 3;
    
    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Relationships
    @Column(nullable = false)
    private String userId;
    
    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FileMetadata metadata;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProcessingLog> progressLogs;
    
    public enum ProcessingStatus {
        QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED, PAUSED
    }
} 