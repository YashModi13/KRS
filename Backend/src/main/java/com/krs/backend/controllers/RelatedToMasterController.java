package com.krs.backend.controllers;

import com.krs.backend.models.RelatedToMaster;
import com.krs.backend.services.RelatedToMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/related-to-masters")
@RequiredArgsConstructor
public class RelatedToMasterController {

    private final RelatedToMasterService relatedToMasterService;

    @GetMapping
    public ResponseEntity<List<RelatedToMaster>> getAll() {
        return ResponseEntity.ok(relatedToMasterService.getAllRelatedToMasters());
    }

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<RelatedToMaster>> getPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return ResponseEntity.ok(relatedToMasterService.getPaginated(search, pageable));
    }

    @PostMapping
    public ResponseEntity<RelatedToMaster> createOrGet(@RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        return ResponseEntity.ok(relatedToMasterService.getOrCreateRelatedTo(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RelatedToMaster> update(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String name = payload != null ? payload.get("name") : null;
        return ResponseEntity.ok(relatedToMasterService.updateRelatedTo(id, name));
    }
}
