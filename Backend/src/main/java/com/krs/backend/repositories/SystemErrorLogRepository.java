package com.krs.backend.repositories;

import com.krs.backend.models.SystemErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemErrorLogRepository extends JpaRepository<SystemErrorLog, Long>, JpaSpecificationExecutor<SystemErrorLog> {
    Page<SystemErrorLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
