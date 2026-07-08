package com.taskOrchestrator.app.job.repository;

import com.taskOrchestrator.app.job.dto.StatusSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.*;

import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;

import java.util.List;

// jpa repo providing save, findAll, delete and pagination JpaSpecificationExecutor for filtering, searching, sorting, composable queries
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT new com.taskOrchestrator.app.job.dto.StatusSummary(j.status, COUNT(j)) FROM Job j " +
            "WHERE (j.user.username = :username)" +
            "AND (j.deleted IS NULL OR j.deleted != true) GROUP BY j.status")
    List<StatusSummary> getStatusSummaryByUser(String username);

    @Query("SELECT new com.taskOrchestrator.app.job.dto.StatusSummary(j.status, COUNT(j)) FROM Job j WHERE j.deleted IS NULL OR j.deleted != true GROUP BY j.status")
    List<StatusSummary> getStatusSummary();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deleted IS NULL OR j.deleted != true")
    long countActive();

    @Query("SELECT COUNT(j) FROM Job j WHERE (j.deleted IS NULL OR j.deleted != true) " +
            "AND (j.user.username = :username) ")
    long countActiveByUser(String username);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status " +
            "AND (j.deleted IS NULL OR j.deleted != true)")
    long countActiveByStatus(JobStatus status);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status " +
            "AND (j.user.username = :username) " +
            "AND (j.deleted IS NULL OR j.deleted != true)")
    long countActiveByUserAndStatus(JobStatus status, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.status = :status ORDER BY j.createdAt ASC")
    List<Job> findNextJobsForUpdate(JobStatus status, PageRequest pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.status = :status " +
            "AND (j.user.username = :username)" +
            "ORDER BY j.createdAt ASC")
    List<Job> findNextJobsForUpdateByUser(JobStatus status, PageRequest pageable, String username);

    //Queued to running
    @Modifying
    @Query("UPDATE Job j SET j.status = 'RUNNING' WHERE j.id IN (SELECT j2.id FROM Job j2 WHERE j2.status = 'QUEUED' ORDER BY j2.createdAt ASC)")
    int claimJobs();

    //realistic simulation: worker throughput, queue limits, batch consumption
    List<Job> findTop5ByStatusOrderByCreatedAtAsc(JobStatus status);
}
