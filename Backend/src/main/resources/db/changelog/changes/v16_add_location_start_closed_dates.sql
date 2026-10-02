-- liquibase formatted sql

-- changeset Antigravity:26-add-location-start-closed-dates
-- validCheckSum: ANY
-- comment: Add start_date and closed_date to project_locations table for site level date tracking

ALTER TABLE krs_schema.project_locations
ADD COLUMN IF NOT EXISTS start_date DATE,
ADD COLUMN IF NOT EXISTS closed_date DATE;
