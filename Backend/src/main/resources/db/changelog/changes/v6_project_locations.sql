-- liquibase formatted sql

-- changeset Antigravity:16-create-project-locations
-- validCheckSum: ANY
-- comment: Create project_locations table for multiple construction sites per project

CREATE TABLE IF NOT EXISTS krs_schema.project_locations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES krs_schema.projects(id) ON DELETE CASCADE,
    block VARCHAR(255),
    school_id VARCHAR(100),
    school_name VARCHAR(500),
    head VARCHAR(100),
    repairing VARCHAR(100),
    new_acr VARCHAR(100),
    new_mdm_sqm VARCHAR(100),
    new_cw_rmt VARCHAR(100),
    gtb VARCHAR(100),
    btb VARCHAR(100),
    cwsn_toilet VARCHAR(100),
    shed VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
