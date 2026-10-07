package com.krs.backend.repositories;

import com.krs.backend.models.ProjectTimeLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTimeLimitRepository extends JpaRepository<ProjectTimeLimit, Long> {
    List<ProjectTimeLimit> findByProjectIdOrderBySortOrderAsc(Long projectId);
    void deleteByProjectId(Long projectId);
}
