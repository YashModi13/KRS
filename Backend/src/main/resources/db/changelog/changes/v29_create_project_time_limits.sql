-- Liquibase Migration: v29_create_project_time_limits.sql
CREATE TABLE IF NOT EXISTS krs_schema.project_time_limits (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    scope VARCHAR(2000),
    duration INT,
    unit VARCHAR(50),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_time_limits_project FOREIGN KEY (project_id) REFERENCES krs_schema.projects(id) ON DELETE CASCADE
);
