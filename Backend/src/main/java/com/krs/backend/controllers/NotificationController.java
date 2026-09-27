package com.krs.backend.controllers;

import com.krs.backend.models.Notification;
import com.krs.backend.models.User;
import com.krs.backend.models.UserNotificationRead;
import com.krs.backend.repositories.NotificationRepository;
import com.krs.backend.repositories.UserRepository;
import com.krs.backend.repositories.UserNotificationReadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationReadRepository userNotificationReadRepository;

    @Autowired
    private UserRepository userRepository;

    public static class NotificationDTO {
        public Long id;
        public String message;
        public String type;
        public boolean isRead;
        public LocalDateTime createdAt;
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long currentUserId = 1L;
        long totalNotifications = notificationRepository.count();
        long readNotifications = userNotificationReadRepository.countByUserId(currentUserId);
        return ResponseEntity.ok(Math.max(0, totalNotifications - readNotifications));
    }

    @GetMapping
    public List<NotificationDTO> getNotifications(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        Long currentUserId = 1L; // Hardcoded to superadmin for now
        List<Notification> paginatedNotifications = notificationRepository.findWithLimitAndOffset(limit, offset);
        
        Set<Long> readNotificationIds = userNotificationReadRepository.findByUserId(currentUserId)
                .stream()
                .map(unr -> unr.getNotification().getId())
                .collect(Collectors.toSet());

        return paginatedNotifications.stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.id = n.getId();
            dto.message = n.getMessage();
            dto.type = n.getType();
            dto.createdAt = n.getCreatedAt();
            dto.isRead = readNotificationIds.contains(n.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Long currentUserId = 1L;
        if (!userNotificationReadRepository.existsByUserIdAndNotificationId(currentUserId, id)) {
            User user = userRepository.findById(currentUserId).orElseThrow();
            Notification notification = notificationRepository.findById(id).orElseThrow();
            
            UserNotificationRead read = new UserNotificationRead();
            read.setUser(user);
            read.setNotification(notification);
            userNotificationReadRepository.save(read);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        Long currentUserId = 1L;
        User user = userRepository.findById(currentUserId).orElseThrow();
        
        List<Notification> allNotifications = notificationRepository.findAll();
        Set<Long> readNotificationIds = userNotificationReadRepository.findByUserId(currentUserId)
                .stream()
                .map(unr -> unr.getNotification().getId())
                .collect(Collectors.toSet());

        List<UserNotificationRead> newReads = allNotifications.stream()
                .filter(n -> !readNotificationIds.contains(n.getId()))
                .map(n -> {
                    UserNotificationRead read = new UserNotificationRead();
                    read.setUser(user);
                    read.setNotification(n);
                    return read;
                }).collect(Collectors.toList());
                
        if (!newReads.isEmpty()) {
            userNotificationReadRepository.saveAll(newReads);
        }
        return ResponseEntity.ok().build();
    }
}
