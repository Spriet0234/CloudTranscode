package com.mediatranscoder.service;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscodingService {

    private final FileStorageService fileStorageService;
    private final ImageProcessingService imageProcessingService;
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    ));

    @Transactional
    public void transcode(Job job) throws IOException {
        log.info("Starting transcoding for job: {}", job.getId());
        
        // Download the original file
        File inputFile = fileStorageService.downloadFile(job.getOriginalFileKey());
        File outputFile = File.createTempFile("processed-", "." + job.getOutputFormat());

        try {
            if (isImageFile(job.getOriginalFilename())) {
                log.info("Processing image file: {}", job.getOriginalFilename());
                imageProcessingService.processImage(inputFile, outputFile, job);
            } else {
                log.info("Processing video file: {}", job.getOriginalFilename());
                transcodeFile(inputFile, outputFile, job);
            }

            // Upload the processed file
            String processedFileKey = fileStorageService.uploadFile(outputFile, job.getOutputFormat());
            job.setProcessedFileKey(processedFileKey);
            job.setStatus(JobStatus.COMPLETED);
            
            log.info("Transcoding completed for job: {}", job.getId());
        } catch (Exception e) {
            log.error("Error during transcoding for job: {}", job.getId(), e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // Clean up temporary files
            if (inputFile != null && inputFile.exists()) {
                inputFile.delete();
            }
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    private boolean isImageFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    private void transcodeFile(File inputFile, File outputFile, Job job) throws IOException {
        // Existing video transcoding logic
        // ... existing code ...
    }
} 