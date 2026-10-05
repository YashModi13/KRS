package com.krs.backend.repositories;

import com.krs.backend.models.BulkUploadProjectsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkUploadProjectsDataRepository extends JpaRepository<BulkUploadProjectsData, Long> {

    List<BulkUploadProjectsData> findByMasterIdOrderByIdAsc(Long masterId);

    List<BulkUploadProjectsData> findByMasterIdAndIsFailedOrderByIdAsc(Long masterId, Boolean isFailed);
}
