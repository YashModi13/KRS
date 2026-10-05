-- liquibase formatted sql

-- changeset Antigravity:30-create-system-error-logs
-- validCheckSum: ANY
-- comment: Create system_error_logs table for global exception audit tracking

CREATE TABLE IF NOT EXISTS krs_schema.system_error_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_type VARCHAR(255) NOT NULL,
    message TEXT,
    stack_trace TEXT,
    endpoint VARCHAR(500),
    http_method VARCHAR(10),
    user_name VARCHAR(255),
    status_code INT DEFAULT 500,
    client_ip VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
