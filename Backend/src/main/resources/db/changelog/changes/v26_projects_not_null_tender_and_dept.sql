-- liquibase formatted sql

-- changeset Antigravity:36-projects-not-null-tender-and-dept splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Delete rows in projects table where tender_id or department_name_id is null/empty and add NOT NULL constraints.

DO $$
BEGIN
    -- 1. Remove dependent child records for projects that have NULL/empty tender_id or department_name_id
    DELETE FROM krs_schema.project_locations 
    WHERE project_id IN (
        SELECT id FROM krs_schema.projects 
        WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = ''
    );

    DELETE FROM krs_schema.ra_bills 
    WHERE project_id IN (
        SELECT id FROM krs_schema.projects 
        WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = ''
    );

    DELETE FROM krs_schema.approvals 
    WHERE project_id IN (
        SELECT id FROM krs_schema.projects 
        WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = ''
    );

    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'krs_schema' AND table_name = 'daily_tasks'
    ) THEN
        DELETE FROM krs_schema.daily_tasks 
        WHERE project_id IN (
            SELECT id FROM krs_schema.projects 
            WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = ''
        );
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'krs_schema' AND table_name = 'project_documents'
    ) THEN
        DELETE FROM krs_schema.project_documents 
        WHERE project_id IN (
            SELECT id FROM krs_schema.projects 
            WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = ''
        );
    END IF;

    -- 2. Delete parent project rows
    DELETE FROM krs_schema.projects 
    WHERE tender_id IS NULL OR TRIM(tender_id) = '' OR department_name_id IS NULL OR TRIM(department_name_id) = '';

    -- 3. Set NOT NULL on tender_id and department_name_id columns
    ALTER TABLE krs_schema.projects ALTER COLUMN tender_id SET NOT NULL;
    ALTER TABLE krs_schema.projects ALTER COLUMN department_name_id SET NOT NULL;
END $$;
