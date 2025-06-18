package com.mediatranscoder.controller;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.repository.JobRepository;
import com.mediatranscoder.service.FileStorageService;
import com.mediatranscoder.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final FileStorageService fileStorageService;
    private final JobRepository jobRepository;

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
    public ResponseEntity<byte[]> getDownloadUrl(@PathVariable UUID jobId) {
        Job job = jobService.getJob(jobId);
        System.out.println("[DEBUG] Download requested for jobId: " + jobId);
        if (job == null) {
            System.out.println("[DEBUG] Job not found for jobId: " + jobId);
            return ResponseEntity.notFound().build();
        }
        System.out.println("[DEBUG] Job found: " + job);
        if (job.getProcessedFileKey() == null) {
            System.out.println("[DEBUG] Processed file key is null for jobId: " + jobId);
            return ResponseEntity.notFound().build();
        }
        try {
            System.out.println("[DEBUG] Downloading processed file from Firebase: " + job.getProcessedFileKey());
            File file = fileStorageService.downloadFile(job.getProcessedFileKey());
            System.out.println("[DEBUG] Downloaded file path: " + file.getAbsolutePath() + ", size: " + file.length());
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String extension = job.getOutputFormat().toLowerCase();
            String baseName = job.getOriginalFilename();
            if (baseName.contains(".")) {
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            }
            String filename = baseName + "." + extension;
            String contentType = extension.equals("jpg") || extension.equals("jpeg")
                ? "image/jpeg"
                : "image/" + extension;
            System.out.println("[DEBUG] Download filename: " + filename);
            System.out.println("[DEBUG] Content type: " + contentType);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            System.out.println("[DEBUG] IOException during download: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download-proxy")
    public ResponseEntity<byte[]> downloadProxy(@RequestParam String url) {
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return ResponseEntity.status(connection.getResponseCode()).build();
            }

            byte[] fileBytes = connection.getInputStream().readAllBytes();
            String contentType = connection.getContentType();
            String filename = url.substring(url.lastIndexOf('/') + 1);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID jobId) {
        try {
            jobService.deleteJob(jobId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/worker-callback")
    public ResponseEntity<String> workerCallback(@RequestBody Map<String, Object> callback) {
        try {
            String jobId = (String) callback.get("job_id");
            String status = (String) callback.get("status");
            String processedFileKey = (String) callback.get("processed_file_key");
            String errorMessage = (String) callback.get("error_message");
            
            log.info("Received worker callback for job {}: status={}", jobId, status);
            
            Job job = jobService.getJob(UUID.fromString(jobId));
            
            if ("completed".equals(status)) {
                job.setStatus(JobStatus.COMPLETED);
                job.setProcessedFileKey(processedFileKey);
                job.setCompletedAt(java.time.LocalDateTime.now());
            } else if ("failed".equals(status)) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage(errorMessage);
            }
            
            jobRepository.save(job);
            log.info("Updated job {} status to {}", jobId, status);
            
            return ResponseEntity.ok("Job status updated");
        } catch (Exception e) {
            log.error("Error processing worker callback", e);
            return ResponseEntity.status(500).body("Error processing callback");
        }
    }
} 