package com.mediatranscoder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private Job mockJob;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        jobId = UUID.randomUUID();
        mockJob = new Job();
        mockJob.setId(jobId);
        mockJob.setOriginalFilename("test-image.jpg");
        mockJob.setOriginalFileKey("test-key/test-image.jpg");
        mockJob.setOutputFormat("jpg");
        mockJob.setOutputQuality("high");
        mockJob.setStatus(JobStatus.QUEUED);
        mockJob.setCreatedAt(LocalDateTime.now());
        mockJob.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetJob_Success() throws Exception {
        // Arrange
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(jobId.toString()))
            .andExpect(jsonPath("$.originalFilename").value("test-image.jpg"))
            .andExpect(jsonPath("$.outputFormat").value("jpg"))
            .andExpect(jsonPath("$.outputQuality").value("high"))
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testGetJob_NotFound() throws Exception {
        // Arrange
        when(jobService.getJob(jobId)).thenThrow(new RuntimeException("Job not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetJobs_Success() throws Exception {
        // Arrange
        List<Job> mockJobs = Arrays.asList(mockJob);
        when(jobService.getJobsByStatus(JobStatus.QUEUED)).thenReturn(mockJobs);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs")
                .param("status", "QUEUED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(jobId.toString()))
            .andExpect(jsonPath("$[0].status").value("QUEUED"));
    }

    @Test
    void testGetJobs_AllStatuses() throws Exception {
        // Arrange
        List<Job> mockJobs = Arrays.asList(mockJob);
        when(jobService.getJobsByStatus(null)).thenReturn(mockJobs);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(jobId.toString()));
    }

    @Test
    void testGetQueuedJobs_Success() throws Exception {
        // Arrange
        List<Job> mockJobs = Arrays.asList(mockJob);
        when(jobService.getQueuedJobs()).thenReturn(mockJobs);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/queued"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(jobId.toString()))
            .andExpect(jsonPath("$[0].status").value("QUEUED"));
    }

    @Test
    void testDeleteJob_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/jobs/{jobId}", jobId))
            .andExpect(status().isOk())
            .andExpect(content().string("Job deleted successfully"));
    }

    @Test
    void testWorkerCallback_Success() throws Exception {
        // Arrange
        mockJob.setStatus(JobStatus.COMPLETED);
        mockJob.setProcessedFileKey("processed/test/output.jpg");
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        String callbackData = """
            {
                "job_id": "%s",
                "status": "completed",
                "processed_file_key": "processed/test/output.jpg",
                "error_message": null
            }
            """.formatted(jobId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/worker-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackData))
            .andExpect(status().isOk())
            .andExpect(content().string("Job updated successfully"));
    }

    @Test
    void testWorkerCallback_Failed() throws Exception {
        // Arrange
        mockJob.setStatus(JobStatus.FAILED);
        mockJob.setErrorMessage("FFmpeg processing failed");
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        String callbackData = """
            {
                "job_id": "%s",
                "status": "failed",
                "processed_file_key": null,
                "error_message": "FFmpeg processing failed"
            }
            """.formatted(jobId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/worker-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackData))
            .andExpect(status().isOk())
            .andExpect(content().string("Job updated successfully"));
    }

    @Test
    void testWorkerCallback_InvalidJobId() throws Exception {
        // Arrange
        String callbackData = """
            {
                "job_id": "invalid-uuid",
                "status": "completed",
                "processed_file_key": "processed/test/output.jpg",
                "error_message": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/worker-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackData))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testWorkerCallback_MissingJobId() throws Exception {
        // Arrange
        String callbackData = """
            {
                "status": "completed",
                "processed_file_key": "processed/test/output.jpg",
                "error_message": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/worker-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackData))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testWorkerCallback_InvalidStatus() throws Exception {
        // Arrange
        String callbackData = """
            {
                "job_id": "%s",
                "status": "invalid_status",
                "processed_file_key": "processed/test/output.jpg",
                "error_message": null
            }
            """.formatted(jobId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/worker-callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackData))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadJob_Success() throws Exception {
        // Arrange
        mockJob.setStatus(JobStatus.COMPLETED);
        mockJob.setProcessedFileKey("processed/test/output.jpg");
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{jobId}/download", jobId))
            .andExpect(status().isOk());
    }

    @Test
    void testDownloadJob_NotCompleted() throws Exception {
        // Arrange
        mockJob.setStatus(JobStatus.QUEUED);
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{jobId}/download", jobId))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Job is not completed"));
    }

    @Test
    void testDownloadJob_NoProcessedFile() throws Exception {
        // Arrange
        mockJob.setStatus(JobStatus.COMPLETED);
        mockJob.setProcessedFileKey(null);
        when(jobService.getJob(jobId)).thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{jobId}/download", jobId))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("No processed file available"));
    }
} 