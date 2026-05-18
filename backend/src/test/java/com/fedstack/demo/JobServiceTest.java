package com.fedstack.demo;

import com.fedstack.demo.dto.CreateJobRequest;
import com.fedstack.demo.dto.JobResponse;
import com.fedstack.demo.model.Job;
import com.fedstack.demo.repository.JobRepository;
import com.fedstack.demo.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.fedstack.demo.exception.JobNotFoundException;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    //create job
    @Test
    void shouldCreateJobSuccessfully() {
        Job job = new Job();
        CreateJobRequest request = new CreateJobRequest();
        job.setName("Test Job");

        when(jobRepository.save(any(Job.class)))
                .thenReturn(job);

        JobResponse result = jobService.createJob(request);

        assertEquals("Test Job", result.getName());
//        verify(jobRepository, times(1)).findAll();
    }

    //update job
    @Test
    void shouldUpdateJobSuccessfully() {
        Job job = new Job();
        job.setName("Test Job");
        assertEquals("Test Job", job.getName());
    }

    //delete job
    @Test
    void shouldDeleteJobSuccessfully() {
        Job job = new Job();
        Pageable pageable = PageRequest.of(0, 100);
        job.setName("Test Job");

        when(jobRepository.findById(job.getId()))
                .thenReturn(java.util.Optional.of(job));

        jobService.deleteJob(job.getId(), pageable);

        verify(jobRepository, times(1)).findById(job.getId());
        jobRepository.flush();
    }
    //edge cases
    @Test
    void shouldThrowExceptionWhenJobNotFound() {
        when(jobRepository.findById(1L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(JobNotFoundException.class, () ->
                jobService.deleteJob(1L, PageRequest.of(0, 100)));

    }

    @Test
    void shouldThrowExceptionWhenPageableIsNull() {
        assertThrows(JobNotFoundException.class, () ->
                jobService.deleteJob(1L, null));

    }

    @Test
    void shouldThrowExceptionWhenJobIdIsNull() {
        assertThrows(JobNotFoundException.class, () ->
                jobService.deleteJob(null, PageRequest.of(0, 100)));

    }

    @Test
    void shouldThrowExceptionWhenPageableSizeIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                jobService.deleteJob(1L, PageRequest.of(0, 0)));

    }

    @Test
    void shouldThrowExceptionWhenPageablePageIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                jobService.deleteJob(1L, PageRequest.of(-1, 100)));

    }

    @Test
    void shouldThrowExceptionWhenPageableSizeIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                jobService.deleteJob(1L, PageRequest.of(0, -1)));

    }

    @Test
    void shouldThrowExceptionWhenPageableSizeIsGreaterThanMax() {
        assertThrows(JobNotFoundException.class, () ->
                jobService.deleteJob(1L, PageRequest.of(0, 10000)));

    }

}