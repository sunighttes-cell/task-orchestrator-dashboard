package com.fedstack.demo.repository;
import com.fedstack.demo.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ExecutionRepository extends JpaRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {
    @Query("SELECT AVG(e.durationMs) FROM Execution e")
    Double averageExecutionTime();
}
