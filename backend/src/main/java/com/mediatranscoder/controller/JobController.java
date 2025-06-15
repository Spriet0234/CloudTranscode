package com.mediatranscoder.controller;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.service.FileStorageService;
import com.mediatranscoder.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<Job> createJob(
            @RequestParam("file") MultipartFile file,
            @RequestParam("outputFormat") String outputFormat,
            @RequestParam("outputQuality") String outputQuality,
            @RequestParam(value = "settings", required = false) Map<String, String> settings) {
        try {
            Job job = jobService.createJob(file, outputFormat, outputQuality, settings);
            return ResponseEntity.ok(job);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJob(@PathVariable UUID jobId) {
        try {
            Job job = jobService.getJob(jobId);
            return ResponseEntity.ok(job);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable JobStatus status) {
        List<Job> jobs = jobService.getJobsByStatus(status);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/queued")
    public ResponseEntity<List<Job>> getQueuedJobs() {
        List<Job> jobs = jobService.getQueuedJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<String> getDownloadUrl(@PathVariable UUID jobId) {
        Job job = jobService.getJob(jobId);
        if (job == null || job.getProcessedFileKey() == null) {
            return ResponseEntity.notFound().build();
        }
        String url = fileStorageService.getFileUrl(job.getProcessedFileKey());
        return ResponseEntity.ok(url);
    }
} 