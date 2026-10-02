-- liquibase formatted sql
-- changeset dev:v15-add-user-theme-preference
ALTER TABLE krs_schema.users ADD COLUMN IF NOT EXISTS theme VARCHAR(20) DEFAULT 'light';
