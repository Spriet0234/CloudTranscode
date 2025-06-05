package com.mediatranscode.service;

import com.mediatranscode.dto.SystemStatsResponse;
import com.mediatranscode.entity.ProcessingJob;
import com.mediatranscode.repository.ProcessingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemStatsService {
    
    private final ProcessingJobRepository jobRepository;
    
    public SystemStatsResponse getSystemStats() {
        try {
            // Get system information
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // Mock data for demonstration - in production, you'd get real metrics
            SystemStatsResponse stats = new SystemStatsResponse();
            
            // Database stats
            stats.setTotalFilesProcessed(jobRepository.countCompletedJobs());
            stats.setActiveUsers(5L); // Mock data
            stats.setTotalProcessingTime(calculateTotalProcessingTime());
            stats.setStorageUsed(1024L * 1024L * 1024L * 2L); // 2GB mock
            
            // System resource stats (mock values for demonstration)
            stats.setCpuUsage(Math.random() * 100);
            stats.setMemoryUsage(getMemoryUsagePercentage());
            stats.setStorageUsage(Math.random() * 100);
            stats.setNetworkIO(Math.random() * 100);
            
            // Processing stats
            stats.setFilesInQueue(jobRepository.countByStatus(ProcessingJob.JobStatus.QUEUED) + 
                                 jobRepository.countByStatus(ProcessingJob.JobStatus.PROCESSING));
            
            Double avgTime = jobRepository.getAverageProcessingTime();
            stats.setAvgProcessingTime(avgTime != null ? avgTime / 1000.0 / 60.0 : 0.0); // Convert to minutes
            
            long total = jobRepository.count();
            long completed = jobRepository.countCompletedJobs();
            stats.setSuccessRate(total > 0 ? (double) completed / total * 100 : 0.0);
            
            stats.setUptime(ManagementFactory.getRuntimeMXBean().getUptime() / 1000); // seconds
            
            // Recent activity (mock data)
            stats.setRecentActivity(Arrays.asList(
                new SystemStatsResponse.ActivityResponse("upload", "User uploaded video.mp4", "2 minutes ago", "success"),
                new SystemStatsResponse.ActivityResponse("processing", "Started processing audio.wav", "5 minutes ago", "processing"),
                new SystemStatsResponse.ActivityResponse("completed", "Completed processing image.jpg", "8 minutes ago", "success"),
                new SystemStatsResponse.ActivityResponse("error", "Failed to process corrupt.mp4", "12 minutes ago", "error")
            ));
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error generating system stats: {}", e.getMessage(), e);
            return createMockStats();
        }
    }
    
    private long calculateTotalProcessingTime() {
        try {
            // This would aggregate processing times from all completed jobs
            return jobRepository.countCompletedJobs() * 120; // Mock: 2 minutes per job on average
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private double getMemoryUsagePercentage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            return max > 0 ? (double) used / max * 100 : 0.0;
        } catch (Exception e) {
            return Math.random() * 100; // Fallback to mock data
        }
    }
    
    private SystemStatsResponse createMockStats() {
        // Fallback mock data if database is not available
        SystemStatsResponse stats = new SystemStatsResponse();
        stats.setTotalFilesProcessed(1247L);
        stats.setActiveUsers(12L);
        stats.setTotalProcessingTime(3600L);
        stats.setStorageUsed(2L * 1024L * 1024L * 1024L);
        stats.setCpuUsage(45.5);
        stats.setMemoryUsage(62.3);
        stats.setStorageUsage(78.1);
        stats.setNetworkIO(34.2);
        stats.setFilesInQueue(8L);
        stats.setAvgProcessingTime(3.2);
        stats.setSuccessRate(98.5);
        stats.setUptime(432000L);
        
        stats.setRecentActivity(Arrays.asList(
            new SystemStatsResponse.ActivityResponse("upload", "User uploaded video.mp4", "2 minutes ago", "success"),
            new SystemStatsResponse.ActivityResponse("processing", "Started processing audio.wav", "5 minutes ago", "processing"),
            new SystemStatsResponse.ActivityResponse("completed", "Completed processing image.jpg", "8 minutes ago", "success"),
            new SystemStatsResponse.ActivityResponse("error", "Failed to process corrupt.mp4", "12 minutes ago", "error")
        ));
        
        return stats;
    }
} 