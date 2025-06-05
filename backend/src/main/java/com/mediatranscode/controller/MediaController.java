package com.mediatranscode.controller;

import com.mediatranscode.dto.*;
import com.mediatranscode.entity.ProcessingJob;
import com.mediatranscode.service.MediaProcessingService;
import com.mediatranscode.service.SystemStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class MediaController {
    
    private final MediaProcessingService mediaProcessingService;
    private final SystemStatsService systemStatsService;
    
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("settings") String settingsJson) {
        
        try {
            UploadResponse response = mediaProcessingService.uploadFile(file, settingsJson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new UploadResponse(null, file.getOriginalFilename(), "FAILED", 0, file.getSize(), 
                            "Upload failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/processing-jobs")
    public ResponseEntity<Map<String, Object>> getProcessingJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            List<JobResponse> jobs = mediaProcessingService.getAllJobs();
            return ResponseEntity.ok(Map.of(
                    "jobs", jobs,
                    "total", jobs.size(),
                    "page", page,
                    "size", size
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch jobs: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/processing-jobs/{jobId}")
    public ResponseEntity<Map<String, String>> deleteProcessingJob(@PathVariable String jobId) {
        try {
            mediaProcessingService.deleteJob(jobId);
            return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to delete job: " + e.getMessage()));
        }
    }
    
    @GetMapping("/system-stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        try {
            SystemStatsResponse stats = systemStatsService.getSystemStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {
        try {
            JobResponse job = mediaProcessingService.getJob(jobId);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Media Transcoding Service",
                "timestamp", System.currentTimeMillis()
        ));
    }
} 