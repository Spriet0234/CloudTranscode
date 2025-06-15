package com.mediatranscoder.service;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final FileStorageService fileStorageService;
    private final TranscodingService transcodingService;

    @Transactional
    public Job createJob(MultipartFile file, String outputFormat, String outputQuality, Map<String, String> settings) throws IOException {
        log.info("Storing file: {}", file.getOriginalFilename());
        String fileKey = fileStorageService.storeFile(file);

        Job job = new Job();
        job.setOriginalFilename(file.getOriginalFilename());
        job.setOriginalFileKey(fileKey);
        job.setOutputFormat(outputFormat);
        job.setOutputQuality(outputQuality);
        if (settings != null) {
            job.setSettings(settings);
        }
        job.setStatus(JobStatus.QUEUED);

        job = jobRepository.save(job);
        log.info("Job saved to DB: {}", job.getId());
        processJob(job);
        return job;
    }

    @Async
    @Transactional
    public void processJob(Job job) {
        try {
            log.info("Processing job: {}", job.getId());
            job.setStatus(JobStatus.PROCESSING);
            jobRepository.save(job);

            transcodingService.transcode(job);
            job.setCompletedAt(java.time.LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job completed: {}", job.getId());
        } catch (Exception e) {
            log.error("Error processing job: {}", job.getId(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobRepository.save(job);
        }
    }

    public Job getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public List<Job> getQueuedJobs() {
        return jobRepository.findByStatusOrderByCreatedAtAsc(JobStatus.QUEUED);
    }
} 