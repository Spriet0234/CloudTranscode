package com.mediatranscoder.controller;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Job> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputFormat", defaultValue = "mp4") String outputFormat,
            @RequestParam(value = "outputQuality", defaultValue = "medium") String outputQuality) {
        log.info("Received upload request: filename={}, outputFormat={}, outputQuality={}", file.getOriginalFilename(), outputFormat, outputQuality);
        try {
            Job job = jobService.createJob(file, outputFormat, outputQuality, null);
            log.info("Job created successfully: {}", job.getId());
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("Error in uploadFile endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 