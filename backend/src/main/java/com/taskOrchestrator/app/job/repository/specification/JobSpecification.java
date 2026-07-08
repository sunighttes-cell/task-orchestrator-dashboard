package com.taskOrchestrator.app.job.repository.specification;
import com.taskOrchestrator.app.job.model.Job;
import com.taskOrchestrator.app.job.model.JobStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class JobSpecification {
    public static Specification<Job> belongsToUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    //root.get("user") → navigates the relationship .get("username") → accesses field inside User
    //JPA builds the join automatically
    public static Specification<Job> belongsToUser(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("user").get("username"),
                        username
                );
    }

    public static Specification<Job> hasId(Long id) {
        return (root, query, cb) ->
                cb.equal(root.get("id"), id);
    }

    // root = job table, root.get("status") = job.status, criteriaBuilder.equal(...) = WHERE status = ?
    public static Specification<Job> hasStatus(JobStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    //SQL Equivalent WHERE LOWER(name) LIKE '%email%' //OR LOWER(description) LIKE '%email%'
    //provides case-insensitive search, reusable logic, composable filtering
    public static Specification<Job> search(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            likePattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            likePattern
                    )
            );
        };
    }

    public static Specification<Job> hasRetryCount(Integer retryCount) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("retryCount"), retryCount);
    }

    public static Specification<Job> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("deleted")),
                        criteriaBuilder.isFalse(root.get("deleted"))
                );
    }

    public static Specification<Job> createdAfter(LocalDateTime createdAt) {
        return (root, query, criteriaBuilder) -> {
            if (createdAt == null) {
                return null; // Ignore filter if timestamp is null
            }
            // Use criteriaBuilder greaterThan for "after" or criteriaBuilder greaterThanOrEqualTo for "after or at"
            return criteriaBuilder.greaterThan(root.get("createdAt"), createdAt);
        };
    }
}