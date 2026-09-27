package com.krs.backend.controllers;

import com.krs.backend.models.Project;
import com.krs.backend.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager entityManager;

    @GetMapping
    public Map<String, Object> getAllProjects(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Map<String, String> allParams) {
        
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        }
        
        PageRequest pageRequest = PageRequest.of(offset / limit, limit, sort);
        
        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (allParams.containsKey("packageNo") && !allParams.get("packageNo").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("packageNo")), "%" + allParams.get("packageNo").toLowerCase() + "%"));
            }
            if (allParams.containsKey("tenderId") && !allParams.get("tenderId").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("tenderId")), "%" + allParams.get("tenderId").toLowerCase() + "%"));
            }
            if (allParams.containsKey("nameOfWork") && !allParams.get("nameOfWork").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameOfWork")), "%" + allParams.get("nameOfWork").toLowerCase() + "%"));
            }
            if (allParams.containsKey("villageName") && !allParams.get("villageName").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("villageName")), "%" + allParams.get("villageName").toLowerCase() + "%"));
            }
            if (allParams.containsKey("tenderAmtMin") && !allParams.get("tenderAmtMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get("tenderAmtMin"))));
            }
            if (allParams.containsKey("tenderAmtMax") && !allParams.get("tenderAmtMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get("tenderAmtMax"))));
            }
            if (allParams.containsKey("agreementAmtMin") && !allParams.get("agreementAmtMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get("agreementAmtMin"))));
            }
            if (allParams.containsKey("agreementAmtMax") && !allParams.get("agreementAmtMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get("agreementAmtMax"))));
            }
            if (allParams.containsKey("startDateMin") && !allParams.get("startDateMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workOrderDate"), java.time.LocalDate.parse(allParams.get("startDateMin"))));
            }
            if (allParams.containsKey("startDateMax") && !allParams.get("startDateMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workOrderDate"), java.time.LocalDate.parse(allParams.get("startDateMax"))));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        long total = projectRepository.count(spec);
        List<Project> projects = projectRepository.findAll(spec, pageRequest).getContent();
        
        for (Project p : projects) {
            entityManager.detach(p);
            p.setLocations(null); 
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", projects);
        response.put("total", total);
        return response;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        return projectRepository.findById(id).map(project -> {
            project.setWorkOrderNumber(projectDetails.getWorkOrderNumber());
            project.setVillageName(projectDetails.getVillageName());
            project.setSecurityDepositAmount(projectDetails.getSecurityDepositAmount());
            project.setRetentionMoneyPerBill(projectDetails.getRetentionMoneyPerBill());
            project.setExtraExcessAmount(projectDetails.getExtraExcessAmount());
            project.setTimeLimitExtension(projectDetails.getTimeLimitExtension());
            project.setCompletionDateActual(projectDetails.getCompletionDateActual());
            Project updated = projectRepository.save(project);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        return projectRepository.findById(id).map(project -> {
            projectRepository.delete(project);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
