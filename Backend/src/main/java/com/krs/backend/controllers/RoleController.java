package com.krs.backend.controllers;

import com.krs.backend.models.Role;
import com.krs.backend.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        
        int page = offset / limit;
        
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            sortDir.equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC,
            sortBy
        );
        
        Page<Role> rolePage = roleRepository.searchRoles(name, description, status, PageRequest.of(page, limit, sort));
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", rolePage.getContent());
        response.put("total", rolePage.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkNameExists(@RequestParam String name, @RequestParam(required = false) Long excludeId) {
        java.util.Optional<Role> roleOpt = roleRepository.findByNameIgnoreCase(name);
        boolean exists = roleOpt.isPresent() && (excludeId == null || !roleOpt.get().getId().equals(excludeId));
        return ResponseEntity.ok(exists);
    }
    
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        return roleRepository.findById(id).map(role -> {
            if (roleDetails.getName() != null) role.setName(roleDetails.getName());
            if (roleDetails.getDescription() != null) role.setDescription(roleDetails.getDescription());
            if (roleDetails.getIsActive() != null) role.setIsActive(roleDetails.getIsActive());
            return ResponseEntity.ok(roleRepository.save(role));
        }).orElse(ResponseEntity.notFound().build());
    }
}
