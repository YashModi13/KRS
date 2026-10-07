package com.krs.backend.services;

import com.krs.backend.models.RelatedToMaster;
import com.krs.backend.repositories.RelatedToMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelatedToMasterService {

    private final RelatedToMasterRepository relatedToMasterRepository;

    /**
     * Case-insensitive lookup and auto-creation of RelatedTo master entry in Title Case.
     */
    @Transactional
    public RelatedToMaster getOrCreateRelatedTo(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }
        String titleCaseName = toTitleCase(rawName);
        Optional<RelatedToMaster> existing = relatedToMasterRepository.findByNameIgnoreCase(titleCaseName);
        if (existing.isPresent()) {
            return existing.get();
        }
        ZoneId zone = ZoneId.systemDefault();
        RelatedToMaster newRecord = RelatedToMaster.builder()
                .name(titleCaseName)
                .createdAt(LocalDateTime.now(zone))
                .updatedAt(LocalDateTime.now(zone))
                .build();
        return relatedToMasterRepository.save(newRecord);
    }

    public Optional<RelatedToMaster> findById(Long id) {
        if (id == null) return Optional.empty();
        return relatedToMasterRepository.findById(id);
    }

    public List<RelatedToMaster> getAllRelatedToMasters() {
        return relatedToMasterRepository.findAllByOrderByNameAsc();
    }

    public org.springframework.data.domain.Page<RelatedToMaster> getPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return relatedToMasterRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return relatedToMasterRepository.findAll(pageable);
    }

    @Transactional
    public RelatedToMaster updateRelatedTo(Long id, String rawName) {
        RelatedToMaster relatedRecord = relatedToMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Related To Master not found with id: " + id));
        if (rawName != null && !rawName.trim().isEmpty()) {
            relatedRecord.setName(toTitleCase(rawName));
        }
        relatedRecord.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        return relatedToMasterRepository.save(relatedRecord);
    }

    /**
     * Converts a string to Title Case (1st letter capital of each word, rest lowercase).
     * E.g. "gokulbhai" -> "Gokulbhai", "gokul bhai" -> "Gokul Bhai", "KISHORBHAI" -> "Kishorbhai"
     */
    public static String toTitleCase(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text != null ? text.trim() : null;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
            }
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
