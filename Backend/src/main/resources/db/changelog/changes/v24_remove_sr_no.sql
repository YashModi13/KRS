-- liquibase formatted sql

-- changeset Antigravity:34-remove-sr-no-column splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Drop sr_no column from projects and bulk_upload_projects_data tables.

DO $$
BEGIN
    -- Drop sr_no from projects
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'projects' AND column_name = 'sr_no'
    ) THEN
        ALTER TABLE krs_schema.projects DROP COLUMN sr_no;
    END IF;

    -- Drop sr_no from bulk_upload_projects_data
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'bulk_upload_projects_data' AND column_name = 'sr_no'
    ) THEN
        ALTER TABLE krs_schema.bulk_upload_projects_data DROP COLUMN sr_no;
    END IF;
END $$;
