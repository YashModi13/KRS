package com.krs.backend.controllers;

import com.krs.backend.models.DepartmentMaster;
import com.krs.backend.services.DepartmentMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department-masters")
@RequiredArgsConstructor
public class DepartmentMasterController {

    private final DepartmentMasterService departmentMasterService;

    @GetMapping
    public ResponseEntity<List<DepartmentMaster>> getAll() {
        return ResponseEntity.ok(departmentMasterService.getAllDepartmentMasters());
    }

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<DepartmentMaster>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return ResponseEntity.ok(departmentMasterService.getPaginated(search, pageable));
    }

    @PostMapping
    public ResponseEntity<DepartmentMaster> createOrGet(@RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        String cityVillage = payload != null ? payload.get("cityVillage") : null;
        String state = payload != null ? payload.get("state") : null;
        return ResponseEntity.ok(departmentMasterService.getOrCreateDepartment(name, cityVillage, state));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentMaster> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        String cityVillage = payload != null ? payload.get("cityVillage") : null;
        String state = payload != null ? payload.get("state") : null;
        return ResponseEntity.ok(departmentMasterService.updateDepartment(id, name, cityVillage, state));
    }
}
