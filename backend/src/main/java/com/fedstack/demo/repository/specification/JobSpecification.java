import com.fedstack.demo.model.Job;
import com.fedstack.demo.model.JobStatus;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {
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
}