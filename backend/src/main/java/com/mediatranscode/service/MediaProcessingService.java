package com.mediatranscode.service;

import com.mediatranscode.dto.JobResponse;
import com.mediatranscode.dto.UploadResponse;
import com.mediatranscode.entity.ProcessingJob;
import com.mediatranscode.repository.ProcessingJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingService {
    
    private final ProcessingJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    
    public UploadResponse uploadFile(MultipartFile file, String settingsJson) {
        try {
            // Create new processing job
            ProcessingJob job = new ProcessingJob();
            job.setId(UUID.randomUUID().toString());
            job.setFileName(file.getOriginalFilename());
            job.setOriginalFileName(file.getOriginalFilename());
            job.setMimeType(file.getContentType());
            job.setFileSize(file.getSize());
            job.setStatus(ProcessingJob.JobStatus.QUEUED);
            job.setProgress(0);
            job.setSettingsJson(settingsJson);
            job.setCreatedAt(LocalDateTime.now());
            
            // Save to database
            job = jobRepository.save(job);
            
            // TODO: Add file to processing queue
            log.info("File uploaded and queued for processing: {}", file.getOriginalFilename());
            
            return new UploadResponse(
                    job.getId(),
                    job.getFileName(),
                    job.getStatus().name(),
                    job.getProgress(),
                    job.getFileSize(),
                    "File uploaded successfully and queued for processing"
            );
            
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    
    public List<JobResponse> getAllJobs() {
        try {
            List<ProcessingJob> jobs = jobRepository.findAll();
            return jobs.stream()
                    .map(this::convertToJobResponse)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching jobs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch jobs", e);
        }
    }
    
    public JobResponse getJob(String jobId) {
        try {
            ProcessingJob job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            return convertToJobResponse(job);
        } catch (Exception e) {
            log.error("Error fetching job {}: {}", jobId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch job", e);
        }
    }
    
    public void deleteJob(String jobId) {
        try {
            if (!jobRepository.existsById(jobId)) {
                throw new RuntimeException("Job not found");
            }
            jobRepository.deleteById(jobId);
            log.info("Job deleted: {}", jobId);
        } catch (Exception e) {
            log.error("Error deleting job {}: {}", jobId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }
    
    private JobResponse convertToJobResponse(ProcessingJob job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setFileName(job.getFileName());
        response.setStatus(job.getStatus().name());
        response.setProgress(job.getProgress());
        response.setFileSize(job.getFileSize());
        response.setDuration(job.getDuration());
        response.setCreatedAt(job.getCreatedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setDownloadUrl(job.getDownloadUrl());
        return response;
    }
} 