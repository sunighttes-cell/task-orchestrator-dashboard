package com.fedstack.demo.repository;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

//jpa repo providing save, findAll, delete and pagination
//JpaSpecificationExecutor for filtering, searching, sorting, composable queries
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    //used for scheduler processing, queued job lookup
    List<Job> findByStatus(JobStatus status);
}
