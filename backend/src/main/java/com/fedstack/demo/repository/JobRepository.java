package com.fedstack.demo.repository;

import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.dto.StatusSummary;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.*;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// jpa repo providing save, findAll, delete and pagination
// JpaSpecificationExecutor for filtering, searching, sorting, composable queries
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT new com.fedstack.demo.dto.StatusSummary(j.status, COUNT(j)) FROM Job j WHERE j.deleted IS NULL OR j.deleted != true GROUP BY j.status")
//    @Query("SELECT new com.fedstack.demo.dto.StatusSummary(j.status, COUNT(j)) FROM Job j GROUP BY j.status")
    List<StatusSummary> getStatusSummary();

    Page<JobResponse> findByDeletedFalse(Pageable pageable);

    //realistic simulation: worker throughput, queue limits, batch consumption
    List<Job> findTop5ByStatusOrderByCreatedAtAsc(JobStatus status);

    long countByStatus(JobStatus status);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deleted IS NULL OR j.deleted != true")
    long countActive();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status AND (j.deleted IS NULL OR j.deleted != true)")
    long countActiveByStatus(JobStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.status = :status ORDER BY j.createdAt ASC")
    List<Job> findNextJobsForUpdate(JobStatus status, PageRequest pageable);

    //Queued to running
    @Modifying
    @Query("UPDATE Job j SET j.status = 'RUNNING' WHERE j.id IN (SELECT j2.id FROM Job j2 WHERE j2.status = 'QUEUED' ORDER BY j2.createdAt ASC)")
    int claimJobs();

}
