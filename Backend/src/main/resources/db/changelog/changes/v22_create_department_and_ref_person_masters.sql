-- liquibase formatted sql

-- changeset Antigravity:32-create-department-and-ref-person-masters splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Create department_master and ref_person_master tables, populate formatted names from projects, and store master IDs in projects.

-- 1. Department Master
CREATE TABLE IF NOT EXISTS krs_schema.department_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO krs_schema.department_master (name)
SELECT DISTINCT INITCAP(TRIM(department_name))
FROM krs_schema.projects
WHERE department_name IS NOT NULL AND TRIM(department_name) <> ''
ON CONFLICT (name) DO NOTHING;

UPDATE krs_schema.projects p
SET department_name = d.id::text
FROM krs_schema.department_master d
WHERE p.department_name IS NOT NULL 
  AND TRIM(p.department_name) <> ''
  AND LOWER(TRIM(p.department_name)) = LOWER(d.name);

-- 2. Ref Person Master
CREATE TABLE IF NOT EXISTS krs_schema.ref_person_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO krs_schema.ref_person_master (name)
SELECT DISTINCT INITCAP(TRIM(ref_person))
FROM krs_schema.projects
WHERE ref_person IS NOT NULL AND TRIM(ref_person) <> ''
ON CONFLICT (name) DO NOTHING;

UPDATE krs_schema.projects p
SET ref_person = r.id::text
FROM krs_schema.ref_person_master r
WHERE p.ref_person IS NOT NULL 
  AND TRIM(p.ref_person) <> ''
  AND LOWER(TRIM(p.ref_person)) = LOWER(r.name);
