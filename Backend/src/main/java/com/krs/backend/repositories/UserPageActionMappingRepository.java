package com.krs.backend.repositories;

import com.krs.backend.models.UserPageActionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserPageActionMappingRepository extends JpaRepository<UserPageActionMapping, Long> {
    List<UserPageActionMapping> findByUserId(Long userId);
}
