package com.krs.backend.repositories;

import com.krs.backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findAll();

    Optional<Project> findById(Long id);

    boolean existsByTenderId(String tenderId);

    boolean existsByNoticeNo(String noticeNo);

    @Query("SELECT DISTINCT p.tenderId FROM Project p WHERE p.tenderId IS NOT NULL AND p.tenderId != ''")
    Set<String> findAllExistingTenderIds();

    @Query("SELECT DISTINCT p.noticeNo FROM Project p WHERE p.noticeNo IS NOT NULL AND p.noticeNo != ''")
    Set<String> findAllExistingNoticeNos();

    @Query("SELECT DISTINCT p.packageNo FROM Project p WHERE p.packageNo IS NOT NULL AND p.packageNo != ''")
    Set<String> findAllExistingPackageNos();
}
