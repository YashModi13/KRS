package com.krs.backend.controllers;

import com.krs.backend.models.Notification;
import com.krs.backend.models.User;
import com.krs.backend.models.UserNotificationRead;
import com.krs.backend.repositories.NotificationRepository;
import com.krs.backend.repositories.UserRepository;
import com.krs.backend.repositories.UserNotificationReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserNotificationReadRepository userNotificationReadRepository;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            UserNotificationReadRepository userNotificationReadRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userNotificationReadRepository = userNotificationReadRepository;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                User u = userRepository.findUserByUsername(username);
                if (u != null) return u.getId();
            }
        } catch (Exception ignored) {
            // Ignore security context lookup exceptions for unauthenticated context
        }
        return 1L; // Fallback for default user
    }

    public static class NotificationDTO {
        private Long id;
        private String message;
        private String type;
        
        @JsonProperty("isRead")
        private boolean isRead;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        @JsonProperty("isRead")
        public boolean isRead() { return isRead; }

        @JsonProperty("isRead")
        public void setRead(boolean read) { isRead = read; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long currentUserId = getCurrentUserId();
        long totalNotifications = notificationRepository.count();
        long readNotifications = userNotificationReadRepository.countByUserId(currentUserId);
        return ResponseEntity.ok(Math.max(0, totalNotifications - readNotifications));
    }

    @GetMapping
    public List<NotificationDTO> getNotifications(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        Long currentUserId = getCurrentUserId();
        List<Notification> paginatedNotifications = notificationRepository.findWithLimitAndOffset(limit, offset);
        
        Set<Long> readNotificationIds = userNotificationReadRepository.findByUserId(currentUserId)
                .stream()
                .map(unr -> unr.getNotification().getId())
                .collect(Collectors.toSet());

        return paginatedNotifications.stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(n.getId());
            dto.setMessage(n.getMessage());
            dto.setType(n.getType());
            dto.setCreatedAt(n.getCreatedAt());
            dto.setRead(readNotificationIds.contains(n.getId()));
            return dto;
        }).toList();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
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

    @PutMapping("/{id}/unread")
    public ResponseEntity<Void> markAsUnread(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        List<UserNotificationRead> reads = userNotificationReadRepository.findByUserId(currentUserId);
        reads.stream()
                .filter(r -> r.getNotification() != null && r.getNotification().getId().equals(id))
                .forEach(userNotificationReadRepository::delete);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long currentUserId = getCurrentUserId();
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
                }).toList();
                
        if (!newReads.isEmpty()) {
            userNotificationReadRepository.saveAll(newReads);
        }
        return ResponseEntity.ok().build();
    }
}
