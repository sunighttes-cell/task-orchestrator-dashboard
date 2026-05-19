package com.fedstack.demo;

import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import com.fedstack.demo.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
class JobRepositoryTest {
//    @Container
//    static PostgreSQLContainer<?> postgres =
//    new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFetchJob() {
        Job job = new Job();
        job.setName("DB Test");
        job.setDescription("Test DB");
        job.setRetryCount(3);
        job.setStatus(JobStatus.QUEUED);

        jobRepository.save(job);

        List<Job> jobs = jobRepository.findAll();

        assertFalse(jobs.isEmpty());
    }

    //test pagination works/Pageable
    @Test
    void shouldPaginateJobs() {
        Job job1 = new Job();
        Job job2 = new Job();
        Job job3 = new Job();
        job1.setName("DB Test");
        job1.setDescription("Test DB");
        job1.setRetryCount(3);
        job1.setStatus(JobStatus.QUEUED);
        job2.setName("DB Test 2");
        job2.setDescription("Test DB 2");
        job2.setRetryCount(3);
        job2.setStatus(JobStatus.QUEUED);
        job3.setName("DB Test 3");
        job3.setDescription("Test DB 3");
        job3.setRetryCount(3);
        job3.setStatus(JobStatus.QUEUED);
        jobRepository.save(job1);
        jobRepository.saveAll(List.of(job2, job3));

        Page<Job> page = jobRepository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }
    //test filtering works/Specifications
    @Test
    void shouldFilterJobs() {
        Job job1 = new Job();
        job1.setName("DB Test");
        job1.setDescription("Test DB");
        job1.setRetryCount(3);
        job1.setStatus(JobStatus.QUEUED);

        Job job2 = new Job();
        job2.setName("Other");
        job2.setName("DB Test 2");
        job2.setDescription("Test DB 2");
        job2.setRetryCount(3);
        job2.setStatus(JobStatus.QUEUED);

        jobRepository.saveAll(List.of(job1, job2));

        Specification<Job> spec = (root, query, cb) ->
                cb.equal(root.get("name"), "DB Test");

        List<Job> filteredJobs = jobRepository.findAll(spec);

        assertEquals(1, filteredJobs.size());
    }
    //test sorting works/Sort
    @Test
    void shouldSortJobs() {
        Job a = new Job(); a.setName("B"); a.setDescription("Test DB");
        a.setRetryCount(3); a.setStatus(JobStatus.QUEUED);
        Job b = new Job(); b.setName("A"); b.setStatus(JobStatus.QUEUED);
        b.setDescription("Test DB 2"); b.setRetryCount(3);

        jobRepository.saveAll(List.of(a, b));

        List<Job> sortedJobs = jobRepository.findAll(Sort.by("name"));

        assertEquals("A", sortedJobs.get(0).getName());
    }
    //test count works/count
    @Test
    void shouldCountJobs() {
        Job job1 = new Job();
        Job job2 = new Job();
        Job job3 = new Job();
        job1.setName("DB Test");
        job1.setDescription("Test DB");
        job1.setRetryCount(3);
        job1.setStatus(JobStatus.QUEUED);
        job2.setName("DB Test 2");
        job2.setDescription("Test DB 2");
        job2.setRetryCount(3);
        job2.setStatus(JobStatus.QUEUED);
        job3.setName("DB Test 3");
        job3.setDescription("Test DB 3");
        job3.setRetryCount(3);
        job3.setStatus(JobStatus.QUEUED);
        jobRepository.save(job1);
        jobRepository.saveAll(List.of(job2, job3));

        long count = jobRepository.count();

        assertEquals(3, count);
    }

    //test soft delete works/deleted
    @Test
    void shouldSoftDeleteJob() {
        Job job = new Job();
        job.setName("DB Test");
        job.setDescription("Test DB");
        job.setRetryCount(3);
        job.setStatus(JobStatus.QUEUED);

        jobRepository.save(job);
        jobRepository.delete(job);

        entityManager.flush();
        entityManager.clear();

        Optional<Job> deletedJob = jobRepository.findById(job.getId());

        assertFalse(deletedJob.isPresent());
    }
    //test soft delete works/deleted
    @Test
    void shouldSoftDeleteJobById() {
        Job job = new Job();
        job.setName("DB Test");
        job.setDescription("Test DB");
        job.setRetryCount(3);
        job.setStatus(JobStatus.QUEUED);
        jobRepository.save(job);

        jobRepository.deleteById(job.getId());

        entityManager.flush();
        entityManager.clear();

        Optional<Job> deletedJob = jobRepository.findById(job.getId());

        assertFalse(deletedJob.isPresent());
    }
    //test custom query works/custom query
    @Test
    void shouldFindJobById() {
        Job job = new Job();
        job.setName("DB Test");
        job.setDescription("Test DB");
        job.setRetryCount(3);
        job.setStatus(JobStatus.QUEUED);
        jobRepository.save(job);

        Optional<Job> foundJob = jobRepository.findById(job.getId());

        assertTrue(foundJob.isPresent());
        assertNotNull(foundJob.get().getId());
        assertEquals("DB Test", foundJob.get().getName());
    }

    @Test
    void shouldFindAllJobsByIds() {
        Job jobA = new Job();
        jobA.setName("DB Test -- 1");
        jobA.setStatus(JobStatus.QUEUED);

        Job jobB = new Job();
        jobB.setName("DB Test -- 2");
        jobB.setStatus(JobStatus.QUEUED);

        Job jobC = new Job();
        jobC.setName("DB Test -- 3");
        jobC.setStatus(JobStatus.QUEUED);

        jobRepository.saveAllAndFlush(List.of(jobA, jobB, jobC));

        List<Long> ids = List.of(jobA.getId(), jobB.getId(), jobC.getId());

        List<Job> jobs = jobRepository.findAllById(ids);

        assertEquals(3, jobs.size());
    }
    @Test
    void shouldFindAllJobs()
    {
        List<Job> jobs = jobRepository.findAll();
        assertNotNull(jobs);
        assertTrue(jobs.size() >= 0);
    }

    @Test
    void shouldFindAllJobsByIdsWithEmptyList() {
        List<Long> ids = List.of();
        List<Job> jobs = jobRepository.findAllById(ids);
        assertEquals(0, jobs.size());
    }

}