package com.krs.backend.controllers;

import com.krs.backend.models.RefPersonMaster;
import com.krs.backend.services.RefPersonMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ref-person-masters")
@RequiredArgsConstructor
public class RefPersonMasterController {

    private final RefPersonMasterService refPersonMasterService;

    @GetMapping
    public ResponseEntity<List<RefPersonMaster>> getAll() {
        return ResponseEntity.ok(refPersonMasterService.getAllRefPersonMasters());
    }

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<RefPersonMaster>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return ResponseEntity.ok(refPersonMasterService.getPaginated(search, pageable));
    }

    @PostMapping
    public ResponseEntity<RefPersonMaster> createOrGet(@RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        return ResponseEntity.ok(refPersonMasterService.getOrCreateRefPerson(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefPersonMaster> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        return ResponseEntity.ok(refPersonMasterService.updateRefPerson(id, name));
    }
}
