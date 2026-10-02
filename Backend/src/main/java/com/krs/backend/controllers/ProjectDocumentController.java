package com.krs.backend.controllers;

import com.krs.backend.models.ProjectDocument;
import com.krs.backend.services.ProjectDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
public class ProjectDocumentController {

    private final ProjectDocumentService documentService;

    public ProjectDocumentController(ProjectDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{projectId}/documents")
    public ResponseEntity<List<ProjectDocument>> getProjectDocuments(@PathVariable Long projectId) {
        List<ProjectDocument> documents = documentService.getDocumentsByProjectId(projectId);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/{projectId}/documents/upload")
    public ResponseEntity<?> uploadProjectDocument(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam(value = "notes", required = false) String notes,
            Principal principal) {
        try {
            String currentUser = principal != null ? principal.getName() : "System Admin";
            ProjectDocument uploadedDoc = documentService.uploadDocument(projectId, file, documentName, notes, currentUser);
            return ResponseEntity.ok(uploadedDoc);
        } catch (IOException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Failed to upload document: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    @GetMapping("/documents/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            ProjectDocument doc = documentService.getDocumentById(documentId);
            Resource resource = documentService.loadDocumentAsResource(documentId);

            String contentDisposition = "attachment; filename=\"" + doc.getDocumentName() + "\"";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "System Admin";
        documentService.deleteDocument(documentId, currentUser);
        Map<String, Boolean> res = new HashMap<>();
        res.put("success", true);
        return ResponseEntity.ok(res);
    }
}
