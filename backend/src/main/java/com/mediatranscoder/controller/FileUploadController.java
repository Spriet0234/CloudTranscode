package com.mediatranscoder.controller;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Job> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputFormat", defaultValue = "jpg") String outputFormat,
            @RequestParam(value = "outputQuality", defaultValue = "medium") String outputQuality,
            @RequestParam(value = "settings[resize]", required = false) String resize,
            @RequestParam(value = "settings[width]", required = false) String width,
            @RequestParam(value = "settings[height]", required = false) String height) {
        log.info("Received upload request: filename={}, outputFormat={}, outputQuality={}, resize={}, width={}, height={}", 
                file.getOriginalFilename(), outputFormat, outputQuality, resize, width, height);
        try {
            Job job = jobService.createJob(file, outputFormat, outputQuality, Map.of(
                "resize", resize != null ? resize : "false",
                "width", width != null ? width : "",
                "height", height != null ? height : ""
            ));
            log.info("Job created successfully: {}", job.getId());
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("Error in uploadFile endpoint", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 