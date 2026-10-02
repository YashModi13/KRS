package com.krs.backend.repositories;

import com.krs.backend.models.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, Long> {
    List<ProjectDocument> findByProjectIdAndIsActiveTrueOrderByUploadedDateDesc(Long projectId);
}
