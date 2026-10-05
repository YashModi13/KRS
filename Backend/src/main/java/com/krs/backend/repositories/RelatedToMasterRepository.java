package com.krs.backend.repositories;

import com.krs.backend.models.RelatedToMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface RelatedToMasterRepository extends JpaRepository<RelatedToMaster, Long> {
    Optional<RelatedToMaster> findByNameIgnoreCase(String name);
    List<RelatedToMaster> findAllByOrderByNameAsc();
    Page<RelatedToMaster> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
