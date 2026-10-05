package com.krs.backend.repositories;

import com.krs.backend.models.RefPersonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefPersonMasterRepository extends JpaRepository<RefPersonMaster, Long> {
    Optional<RefPersonMaster> findByNameIgnoreCase(String name);
    List<RefPersonMaster> findAllByOrderByNameAsc();
    Page<RefPersonMaster> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
