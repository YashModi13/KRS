package com.krs.backend.repositories;

import com.krs.backend.models.RaBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaBillRepository extends JpaRepository<RaBill, Long> {
    List<RaBill> findByProjectId(Long projectId);
}
