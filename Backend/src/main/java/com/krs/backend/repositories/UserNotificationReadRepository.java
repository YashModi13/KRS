package com.krs.backend.repositories;

import com.krs.backend.models.UserNotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationReadRepository extends JpaRepository<UserNotificationRead, Long> {
    List<UserNotificationRead> findByUserId(Long userId);
    boolean existsByUserIdAndNotificationId(Long userId, Long notificationId);
    long countByUserId(Long userId);
}
