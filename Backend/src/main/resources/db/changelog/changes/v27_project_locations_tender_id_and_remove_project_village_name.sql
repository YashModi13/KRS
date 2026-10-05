-- liquibase formatted sql

-- changeset Antigravity:37-project-locations-tender-id-and-remove-project-village-name splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Add tender_id column to project_locations table and drop village_name from projects table.

DO $$
BEGIN
    -- 1. Add tender_id column to project_locations table if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'project_locations' AND column_name = 'tender_id'
    ) THEN
        ALTER TABLE krs_schema.project_locations ADD COLUMN tender_id VARCHAR(255);
    END IF;

    -- 2. Populate tender_id in project_locations from parent project
    UPDATE krs_schema.project_locations pl
    SET tender_id = p.tender_id
    FROM krs_schema.projects p
    WHERE pl.project_id = p.id AND (pl.tender_id IS NULL OR pl.tender_id = '');

    -- 3. Drop village_name column from projects table if exists
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'krs_schema' AND table_name = 'projects' AND column_name = 'village_name'
    ) THEN
        ALTER TABLE krs_schema.projects DROP COLUMN village_name;
    END IF;
END $$;
