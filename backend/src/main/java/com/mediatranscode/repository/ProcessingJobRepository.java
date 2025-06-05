package com.mediatranscode.repository;

import com.mediatranscode.entity.ProcessingJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, String> {
    
    Page<ProcessingJob> findByStatusOrderByCreatedAtDesc(ProcessingJob.JobStatus status, Pageable pageable);
    
    List<ProcessingJob> findByStatusOrderByCreatedAtDesc(ProcessingJob.JobStatus status);
    
    long countByStatus(ProcessingJob.JobStatus status);
    
    @Query("SELECT COUNT(j) FROM ProcessingJob j WHERE j.status = 'COMPLETED'")
    long countCompletedJobs();
    
    @Query("SELECT COUNT(j) FROM ProcessingJob j WHERE j.status = 'FAILED'")
    long countFailedJobs();
    
    @Query("SELECT AVG(j.processingTime) FROM ProcessingJob j WHERE j.status = 'COMPLETED' AND j.processingTime IS NOT NULL")
    Double getAverageProcessingTime();
} 