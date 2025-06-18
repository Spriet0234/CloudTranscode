package com.mediatranscoder.repository;

import com.mediatranscoder.model.Job;
import com.mediatranscoder.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByStatusOrderByCreatedAtAsc(JobStatus status);
} 