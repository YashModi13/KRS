package com.krs.backend.repositories;

import com.krs.backend.models.DepartmentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentMasterRepository extends JpaRepository<DepartmentMaster, Long> {
    Optional<DepartmentMaster> findByNameIgnoreCase(String name);
    List<DepartmentMaster> findAllByOrderByNameAsc();
    Page<DepartmentMaster> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
