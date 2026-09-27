package com.krs.backend.repositories;

import com.krs.backend.models.RolePageActionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePageActionMappingRepository extends JpaRepository<RolePageActionMapping, Long> {
    List<RolePageActionMapping> findByRoleId(Long roleId);
}
