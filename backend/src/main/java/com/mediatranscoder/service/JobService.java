package com.mediatranscoder.service;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.repository.JobRepository;
import com.mediatranscoder.service.RabbitMQJobProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RabbitMQJobProducer rabbitMQJobProducer;

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

        // Enqueue job to RabbitMQ
        String inputUrl = fileStorageService.getFileUrl(fileKey);
        rabbitMQJobProducer.sendJob(job, inputUrl);

        return job;
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

    public void deleteJob(UUID jobId) {
        jobRepository.deleteById(jobId);
    }
} 