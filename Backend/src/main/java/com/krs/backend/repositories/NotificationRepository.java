package com.krs.backend.repositories;

import com.krs.backend.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = "SELECT * FROM krs_schema.notifications ORDER BY created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Notification> findWithLimitAndOffset(@Param("limit") int limit, @Param("offset") int offset);
}
