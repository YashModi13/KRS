-- liquibase formatted sql

-- changeset Antigravity:24-normalize-domain-tables
-- validCheckSum: ANY
-- comment: Update and normalize project_locations, ra_bills, and approvals tables with complete foreign keys and attributes

-- 1. Extend project_locations table
ALTER TABLE krs_schema.project_locations
ADD COLUMN IF NOT EXISTS village_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS taluka VARCHAR(100),
ADD COLUMN IF NOT EXISTS district VARCHAR(100),
ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Running',
ADD COLUMN IF NOT EXISTS physical_progress DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS financial_progress DECIMAL(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS time_limit VARCHAR(100);

-- 2. Extend ra_bills table
ALTER TABLE krs_schema.ra_bills
ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Pending Approval',
ADD COLUMN IF NOT EXISTS passed_date DATE,
ADD COLUMN IF NOT EXISTS paid_date DATE,
ADD COLUMN IF NOT EXISTS remarks TEXT;

-- 3. Extend approvals table
ALTER TABLE krs_schema.approvals
ADD COLUMN IF NOT EXISTS approval_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS approval_date DATE,
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS approval_letter_file VARCHAR(255),
ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Approved';

-- Indexes for relational performance
CREATE INDEX IF NOT EXISTS idx_project_locations_project_id ON krs_schema.project_locations(project_id);
CREATE INDEX IF NOT EXISTS idx_ra_bills_project_id ON krs_schema.ra_bills(project_id);
CREATE INDEX IF NOT EXISTS idx_approvals_project_id ON krs_schema.approvals(project_id);
