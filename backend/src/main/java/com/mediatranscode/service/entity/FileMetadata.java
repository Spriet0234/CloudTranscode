package com.mediatranscode.service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // General metadata
    private String format;
    private Double duration; // in seconds
    private Integer bitrate;
    
    // Video metadata
    private String videoCodec;
    private String audioCodec;
    private Integer width;
    private Integer height;
    private Double frameRate;
    private String aspectRatio;
    
    // Audio metadata
    private Integer sampleRate;
    private Integer channels;
    
    // Image metadata
    private String colorSpace;
    private Integer quality;
    
    // Additional metadata stored as JSON
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private ProcessingJob job;
} 