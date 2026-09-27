package com.krs.backend.repositories;

import com.krs.backend.models.PageActionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PageActionMappingRepository extends JpaRepository<PageActionMapping, Long> {
    List<PageActionMapping> findByPageId(Long pageId);
}
