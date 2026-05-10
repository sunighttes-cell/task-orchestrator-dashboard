package com.fedstack.demo.repository;

import com.fedstack.demo.dto.StatusSummary;
import org.springframework.data.jpa.repository.Query;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// jpa repo providing save, findAll, delete and pagination
// JpaSpecificationExecutor for filtering, searching, sorting, composable queries
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT new com.fedstack.demo.dto.StatusSummary(j.status, COUNT(j)) FROM Job j GROUP BY j.status")
    List<StatusSummary> getStatusSummary();

    //realistic simulation: worker throughput, queue limits, batch consumption
    List<Job> findTop5ByStatusOrderByCreatedAtAsc(JobStatus status);

    //Equivalent SQL: SELECT COUNT(*) FROM jobs WHERE status = ?
    //summary cards //metrics //monitoring
    long countByStatus(JobStatus status);

    //Equivalent SQL; SELECT * FROM jobs //WHERE status = 'QUEUED' //ORDER BY created_at ASC //LIMIT 10
    //job queue processing //oldest-first orchestration
    List<Job> findTop10ByStatusOrderByCreatedAtAsc(JobStatus status);
}
