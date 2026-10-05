package com.krs.backend.repositories;

import com.krs.backend.models.ProjectUploadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectUploadHistoryRepository extends JpaRepository<ProjectUploadHistory, Long> {

    List<ProjectUploadHistory> findAllByOrderByUploadTimeDesc();
}
