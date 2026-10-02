-- liquibase formatted sql

-- changeset Antigravity:27-create-project-documents-table
-- validCheckSum: ANY
-- comment: Create project_documents table for uploading and managing project files

CREATE TABLE IF NOT EXISTS krs_schema.project_documents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES krs_schema.projects(id) ON DELETE CASCADE,
    document_name VARCHAR(255),
    file_name VARCHAR(255),
    notes TEXT,
    uploaded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location_path VARCHAR(500),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);
