package com.krs.backend.services;

import com.krs.backend.models.Project;
import com.krs.backend.models.ProjectDocument;
import com.krs.backend.repositories.ProjectDocumentRepository;
import com.krs.backend.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ProjectDocumentService {

    private final ProjectDocumentRepository documentRepository;
    private final ProjectRepository projectRepository;

    @Value("${file.upload-dir:uploads/documents}")
    private String uploadDir;

    public ProjectDocumentService(ProjectDocumentRepository documentRepository, ProjectRepository projectRepository) {
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
    }

    public List<ProjectDocument> getDocumentsByProjectId(Long projectId) {
        return documentRepository.findByProjectIdAndIsActiveTrueOrderByUploadedDateDesc(projectId);
    }

    public ProjectDocument uploadDocument(Long projectId, MultipartFile file, String documentName, String notes, String currentUser) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String finalDocName = (documentName != null && !documentName.trim().isEmpty())
                ? documentName.trim()
                : originalFilename;

        String userPrefix = (currentUser != null && !currentUser.trim().isEmpty())
                ? currentUser.trim().replaceAll("[^a-zA-Z0-9]", "_")
                : "user";

        String nameWithoutExt = originalFilename;
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            nameWithoutExt = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex);
        }

        long timestamp = System.currentTimeMillis();
        String baseStoredName = userPrefix + "_" + timestamp + "_" + nameWithoutExt;
        String candidateFileName = baseStoredName + extension;

        Path storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(storageDirectory);

        int duplicateCounter = 1;
        while (Files.exists(storageDirectory.resolve(candidateFileName))) {
            candidateFileName = baseStoredName + "_" + duplicateCounter + extension;
            duplicateCounter++;
        }

        Path targetLocation = storageDirectory.resolve(candidateFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        ZoneId zone = ZoneId.systemDefault();
        ProjectDocument doc = ProjectDocument.builder()
                .project(project)
                .documentName(finalDocName)
                .fileName(candidateFileName)
                .notes(notes)
                .locationPath(targetLocation.toString())
                .uploadedDate(LocalDateTime.now(zone))
                .createdDate(LocalDateTime.now(zone))
                .createdBy(currentUser != null ? currentUser : "System Admin")
                .updateDate(LocalDateTime.now(zone))
                .updatedBy(currentUser != null ? currentUser : "System Admin")
                .isActive(true)
                .build();

        return documentRepository.save(doc);
    }

    public Resource loadDocumentAsResource(Long documentId) throws FileNotFoundException, MalformedURLException {
        ProjectDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));

        Path filePath = Paths.get(doc.getLocationPath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found or not readable: " + doc.getFileName());
        }
    }

    public ProjectDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));
    }

    public void deleteDocument(Long documentId, String currentUser) {
        ProjectDocument doc = getDocumentById(documentId);
        doc.setIsActive(false);
        doc.setUpdateDate(LocalDateTime.now(ZoneId.systemDefault()));
        doc.setUpdatedBy(currentUser != null ? currentUser : "System Admin");
        documentRepository.save(doc);
    }
}
