package com.krs.backend.repositories;
import com.krs.backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByNameIgnoreCase(String name);

    @Query("SELECT r FROM Role r WHERE " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:description AS string), '%'))) AND " +
           "(:status IS NULL OR r.isActive = :status)")
    Page<Role> searchRoles(@Param("name") String name, 
                           @Param("description") String description, 
                           @Param("status") Boolean status,
                           Pageable pageable);
}
