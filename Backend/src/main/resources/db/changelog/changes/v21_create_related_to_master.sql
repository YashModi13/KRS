-- liquibase formatted sql

-- changeset Antigravity:31-create-related-to-master splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Create related_to_master table, populate formatted names from projects, and store master ID in projects.related_to.

CREATE TABLE IF NOT EXISTS krs_schema.related_to_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Populate related_to_master from existing distinct related_to values in projects (formatted in Title Case)
INSERT INTO krs_schema.related_to_master (name)
SELECT DISTINCT INITCAP(TRIM(related_to))
FROM krs_schema.projects
WHERE related_to IS NOT NULL AND TRIM(related_to) <> ''
ON CONFLICT (name) DO NOTHING;

-- Update existing projects.related_to to store the master ID
UPDATE krs_schema.projects p
SET related_to = r.id::text
FROM krs_schema.related_to_master r
WHERE p.related_to IS NOT NULL 
  AND TRIM(p.related_to) <> ''
  AND LOWER(TRIM(p.related_to)) = LOWER(r.name);
