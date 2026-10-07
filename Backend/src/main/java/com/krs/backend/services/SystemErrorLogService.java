package com.krs.backend.services;

import com.krs.backend.models.SystemErrorLog;
import com.krs.backend.repositories.SystemErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemErrorLogService {

    private final SystemErrorLogRepository errorLogRepository;

    /**
     * Log an exception/error asynchronously in a new transaction so it is persisted even if the main transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SystemErrorLog logError(String errorType, String message, Throwable throwable, HttpServletRequest request, Integer statusCode) {
        String stackTrace = null;
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            stackTrace = sw.toString();
        }

        String username = "system";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {
            // Ignore authentication lookup errors when logged in unauthenticated context
        }

        String endpoint = null;
        String httpMethod = null;
        String clientIp = null;

        if (request != null) {
            endpoint = request.getRequestURI();
            httpMethod = request.getMethod();
            clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = request.getRemoteAddr();
            }
        }

        log.error("SYSTEM_ERROR_LOGGED [Type: {}, Endpoint: {}, User: {}, Status: {}] - {}",
                errorType, endpoint, username, statusCode, message, throwable);

        String resolvedErrorType = errorType;
        if (resolvedErrorType == null) {
            resolvedErrorType = (throwable != null) ? throwable.getClass().getSimpleName() : "UnknownError";
        }

        String resolvedMessage = message;
        if (resolvedMessage == null) {
            resolvedMessage = (throwable != null) ? throwable.getMessage() : "No message provided";
        }

        ZoneId zone = ZoneId.systemDefault();

        SystemErrorLog errorLog = SystemErrorLog.builder()
                .timestamp(LocalDateTime.now(zone))
                .errorType(resolvedErrorType)
                .message(resolvedMessage)
                .stackTrace(stackTrace)
                .endpoint(endpoint)
                .httpMethod(httpMethod)
                .userName(username)
                .statusCode(statusCode != null ? statusCode : 500)
                .clientIp(clientIp)
                .createdAt(LocalDateTime.now(zone))
                .build();

        try {
            return errorLogRepository.saveAndFlush(errorLog);
        } catch (Exception ex) {
            log.error("Failed to persist SystemErrorLog to database", ex);
            return errorLog;
        }
    }

    @Transactional(readOnly = true)
    public Page<SystemErrorLog> getErrorLogs(int page, int size, String search, Integer statusCode) {
        Specification<SystemErrorLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (statusCode != null) {
                predicates.add(cb.equal(root.get("statusCode"), statusCode));
            }

            if (search != null && !search.trim().isEmpty()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("errorType")), term),
                        cb.like(cb.lower(root.get("message")), term),
                        cb.like(cb.lower(root.get("endpoint")), term),
                        cb.like(cb.lower(root.get("userName")), term)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return errorLogRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @Transactional
    public void clearAllLogs() {
        errorLogRepository.deleteAll();
    }
}
