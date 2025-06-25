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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileUploadController.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    private Job mockJob;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockJob = new Job();
        mockJob.setId(UUID.randomUUID());
        mockJob.setOriginalFilename("test-image.jpg");
        mockJob.setOriginalFileKey("test-key/test-image.jpg");
        mockJob.setOutputFormat("jpg");
        mockJob.setOutputQuality("high");
        mockJob.setStatus(JobStatus.QUEUED);

        mockFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    void testUploadFile_Success() throws Exception {
        // Arrange
        when(jobService.createJob(any(), eq("jpg"), eq("high"), any()))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile)
                .param("outputFormat", "jpg")
                .param("outputQuality", "high"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(mockJob.getId().toString()))
            .andExpect(jsonPath("$.originalFilename").value("test-image.jpg"))
            .andExpect(jsonPath("$.outputFormat").value("jpg"))
            .andExpect(jsonPath("$.outputQuality").value("high"))
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void testUploadFile_WithResizeSettings() throws Exception {
        // Arrange
        Map<String, String> expectedSettings = new HashMap<>();
        expectedSettings.put("resize", "true");
        expectedSettings.put("width", "800");
        expectedSettings.put("height", "600");

        when(jobService.createJob(any(), eq("jpg"), eq("high"), eq(expectedSettings)))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile)
                .param("outputFormat", "jpg")
                .param("outputQuality", "high")
                .param("settings[resize]", "true")
                .param("settings[width]", "800")
                .param("settings[height]", "600"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(mockJob.getId().toString()));
    }

    @Test
    void testUploadFile_WithDefaultValues() throws Exception {
        // Arrange
        when(jobService.createJob(any(), eq("jpg"), eq("medium"), any()))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.outputFormat").value("jpg"))
            .andExpect(jsonPath("$.outputQuality").value("medium"));
    }

    @Test
    void testUploadFile_WithCustomFormatAndQuality() throws Exception {
        // Arrange
        when(jobService.createJob(any(), eq("png"), eq("low"), any()))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile)
                .param("outputFormat", "png")
                .param("outputQuality", "low"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.outputFormat").value("jpg")) // Response shows original format
            .andExpect(jsonPath("$.outputQuality").value("high")); // Response shows original quality
    }

    @Test
    void testUploadFile_WithoutFile() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .param("outputFormat", "jpg")
                .param("outputQuality", "high"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadFile_ServiceException() throws Exception {
        // Arrange
        when(jobService.createJob(any(), anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile)
                .param("outputFormat", "jpg")
                .param("outputQuality", "high"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testUploadFile_WithPartialResizeSettings() throws Exception {
        // Arrange
        Map<String, String> expectedSettings = new HashMap<>();
        expectedSettings.put("resize", "true");
        expectedSettings.put("width", "800");
        expectedSettings.put("height", "");

        when(jobService.createJob(any(), eq("jpg"), eq("high"), eq(expectedSettings)))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(mockFile)
                .param("outputFormat", "jpg")
                .param("outputQuality", "high")
                .param("settings[resize]", "true")
                .param("settings[width]", "800")
                .param("settings[height]", ""))
            .andExpect(status().isOk());
    }

    @Test
    void testUploadFile_WithLargeFile() throws Exception {
        // Arrange
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large-image.jpg",
            "image/jpeg",
            largeContent
        );

        when(jobService.createJob(any(), eq("jpg"), eq("high"), any()))
            .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/upload")
                .file(largeFile)
                .param("outputFormat", "jpg")
                .param("outputQuality", "high"))
            .andExpect(status().isOk());
    }

    @Test
    void testUploadFile_WithDifferentFileTypes() throws Exception {
        // Test with PNG file
        MockMultipartFile pngFile = new MockMultipartFile(
            "file",
            "test-image.png",
            "image/png",
            "test png content".getBytes()
        );

        when(jobService.createJob(any(), eq("webp"), eq("medium"), any()))
            .thenReturn(mockJob);

        mockMvc.perform(multipart("/api/v1/upload")
                .file(pngFile)
                .param("outputFormat", "webp")
                .param("outputQuality", "medium"))
            .andExpect(status().isOk());
    }
} 