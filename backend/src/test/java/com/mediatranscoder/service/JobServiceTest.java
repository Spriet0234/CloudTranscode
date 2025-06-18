package com.mediatranscoder.service;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import com.mediatranscoder.repository.JobRepository;
import com.mediatranscoder.service.RabbitMQJobProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RabbitMQJobProducer rabbitMQJobProducer;

    @InjectMocks
    private JobService jobService;

    private MockMultipartFile mockFile;
    private Job mockJob;
    private Map<String, String> mockSettings;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        mockJob = new Job();
        mockJob.setId(UUID.randomUUID());
        mockJob.setOriginalFilename("test-image.jpg");
        mockJob.setOriginalFileKey("test-key/test-image.jpg");
        mockJob.setOutputFormat("jpg");
        mockJob.setOutputQuality("high");
        mockJob.setStatus(JobStatus.QUEUED);

        mockSettings = new HashMap<>();
        mockSettings.put("resize", "true");
        mockSettings.put("width", "800");
        mockSettings.put("height", "600");
    }

    @Test
    void testCreateJob_Success() throws IOException {
        // Arrange
        String fileKey = "test-key/test-image.jpg";
        String fileUrl = "https://storage.googleapis.com/test-bucket/" + fileKey;
        
        when(fileStorageService.storeFile(any(MockMultipartFile.class))).thenReturn(fileKey);
        when(fileStorageService.getFileUrl(fileKey)).thenReturn(fileUrl);
        when(jobRepository.save(any(Job.class))).thenReturn(mockJob);

        // Act
        Job result = jobService.createJob(mockFile, "jpg", "high", mockSettings);

        // Assert
        assertNotNull(result);
        assertEquals(mockJob.getId(), result.getId());
        assertEquals("test-image.jpg", result.getOriginalFilename());
        assertEquals(fileKey, result.getOriginalFileKey());
        assertEquals("jpg", result.getOutputFormat());
        assertEquals("high", result.getOutputQuality());
        assertEquals(JobStatus.QUEUED, result.getStatus());
        assertEquals(mockSettings, result.getSettings());

        // Verify interactions
        verify(fileStorageService).storeFile(mockFile);
        verify(fileStorageService).getFileUrl(fileKey);
        verify(jobRepository).save(any(Job.class));
        verify(rabbitMQJobProducer).sendJob(result, fileUrl);
    }

    @Test
    void testCreateJob_WithNullSettings() throws IOException {
        // Arrange
        String fileKey = "test-key/test-image.jpg";
        String fileUrl = "https://storage.googleapis.com/test-bucket/" + fileKey;
        
        when(fileStorageService.storeFile(any(MockMultipartFile.class))).thenReturn(fileKey);
        when(fileStorageService.getFileUrl(fileKey)).thenReturn(fileUrl);
        when(jobRepository.save(any(Job.class))).thenReturn(mockJob);

        // Act
        Job result = jobService.createJob(mockFile, "png", "medium", null);

        // Assert
        assertNotNull(result);
        assertEquals("png", result.getOutputFormat());
        assertEquals("medium", result.getOutputQuality());
        assertNull(result.getSettings());

        verify(rabbitMQJobProducer).sendJob(result, fileUrl);
    }

    @Test
    void testCreateJob_FileStorageException() throws IOException {
        // Arrange
        when(fileStorageService.storeFile(any(MockMultipartFile.class)))
            .thenThrow(new IOException("Storage service error"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            jobService.createJob(mockFile, "jpg", "high", mockSettings);
        });

        verify(jobRepository, never()).save(any(Job.class));
        verify(rabbitMQJobProducer, never()).sendJob(any(Job.class), anyString());
    }

    @Test
    void testGetJob_Success() {
        // Arrange
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(mockJob));

        // Act
        Job result = jobService.getJob(jobId);

        // Assert
        assertNotNull(result);
        assertEquals(mockJob.getId(), result.getId());
        verify(jobRepository).findById(jobId);
    }

    @Test
    void testGetJob_NotFound() {
        // Arrange
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            jobService.getJob(jobId);
        });

        verify(jobRepository).findById(jobId);
    }

    @Test
    void testGetJobsByStatus() {
        // Arrange
        List<Job> mockJobs = Arrays.asList(mockJob);
        when(jobRepository.findByStatus(JobStatus.QUEUED)).thenReturn(mockJobs);

        // Act
        List<Job> result = jobService.getJobsByStatus(JobStatus.QUEUED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockJob.getId(), result.get(0).getId());
        verify(jobRepository).findByStatus(JobStatus.QUEUED);
    }

    @Test
    void testGetQueuedJobs() {
        // Arrange
        List<Job> mockJobs = Arrays.asList(mockJob);
        when(jobRepository.findByStatusOrderByCreatedAtAsc(JobStatus.QUEUED)).thenReturn(mockJobs);

        // Act
        List<Job> result = jobService.getQueuedJobs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockJob.getId(), result.get(0).getId());
        verify(jobRepository).findByStatusOrderByCreatedAtAsc(JobStatus.QUEUED);
    }

    @Test
    void testDeleteJob() {
        // Arrange
        UUID jobId = UUID.randomUUID();

        // Act
        jobService.deleteJob(jobId);

        // Assert
        verify(jobRepository).deleteById(jobId);
    }
} 