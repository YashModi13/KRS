package com.krs.backend.controllers;

import com.krs.backend.models.User;
import com.krs.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        
        int page = offset / limit;
        Long safeRoleId = (roleId == null) ? -1L : roleId;
        
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC,
            sortBy
        );
        
        Page<User> userPage = userRepository.searchUsers(username, email, status, safeRoleId, PageRequest.of(page, limit, sort));
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", userPage.getContent());
        response.put("total", userPage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameExists(@RequestParam String username, @RequestParam(required = false) Long excludeId) {
        User user = userRepository.findByUsernameIgnoreCase(username);
        boolean exists = user != null && (excludeId == null || !user.getId().equals(excludeId));
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email, @RequestParam(required = false) Long excludeId) {
        User user = userRepository.findByEmailIgnoreCase(email);
        boolean exists = user != null && (excludeId == null || !user.getId().equals(excludeId));
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User userDetails) {
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        if (userDetails.getIsActive() == null) {
            userDetails.setIsActive(true);
        }
        return ResponseEntity.ok(userRepository.save(userDetails));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            // Only update fields that are explicitly provided (non-null)
            if (userDetails.getUsername() != null && !userDetails.getUsername().isEmpty()) {
                user.setUsername(userDetails.getUsername());
            }
            if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
                user.setEmail(userDetails.getEmail());
            }
            if (userDetails.getIsActive() != null) {
                user.setIsActive(userDetails.getIsActive());
            }
            if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
                user.setRoles(userDetails.getRoles());
            }
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}
