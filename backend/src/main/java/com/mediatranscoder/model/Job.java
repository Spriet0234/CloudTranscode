package com.mediatranscoder.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, length = 1000)
    private String originalFileKey;

    @Column(length = 1000)
    private String processedFileKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.QUEUED;

    @Column(length = 2000)
    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private String outputFormat;

    @Column(nullable = false)
    private String outputQuality;

    @ElementCollection
    @CollectionTable(name = "job_settings", joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value")
    private Map<String, String> settings = new HashMap<>();
} 