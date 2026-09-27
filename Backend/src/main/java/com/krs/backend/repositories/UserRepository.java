package com.krs.backend.repositories;
import com.krs.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<String> findByUsername(String username);
    User findUserByUsername(String username);
    User findByEmail(String email);
    User findByUsernameIgnoreCase(String username);
    User findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:username AS string), '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%'))) AND " +
           "(:status IS NULL OR u.isActive = :status) AND " +
           "(:roleId = -1L OR :roleId IN (SELECT r.id FROM u.roles r))")
    Page<User> searchUsers(@Param("username") String username, 
                           @Param("email") String email, 
                           @Param("status") Boolean status, 
                           @Param("roleId") Long roleId,
                           Pageable pageable);
}
