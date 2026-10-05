-- liquibase formatted sql

-- changeset Antigravity:33-rename-master-id-columns-in-projects splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Rename master ID columns ONLY in projects table to add _id suffix.

DO $$
BEGIN
    -- Rename department_name to department_name_id in projects
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'projects' AND column_name = 'department_name'
    ) THEN
        ALTER TABLE krs_schema.projects RENAME COLUMN department_name TO department_name_id;
    END IF;

    -- Rename ref_person to ref_person_id in projects
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'projects' AND column_name = 'ref_person'
    ) THEN
        ALTER TABLE krs_schema.projects RENAME COLUMN ref_person TO ref_person_id;
    END IF;

    -- Rename related_to to related_to_id in projects
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'projects' AND column_name = 'related_to'
    ) THEN
        ALTER TABLE krs_schema.projects RENAME COLUMN related_to TO related_to_id;
    END IF;
END $$;
