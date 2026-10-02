-- liquibase formatted sql

-- changeset Antigravity:23-add-tender-details-columns
-- validCheckSum: ANY
-- comment: Add missing columns to projects table from TENDER DETAILS.xlsx

ALTER TABLE krs_schema.projects
ADD COLUMN IF NOT EXISTS sr_no BIGINT,
ADD COLUMN IF NOT EXISTS notice_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS related_to VARCHAR(255),
ADD COLUMN IF NOT EXISTS tender_fee DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS tender_fee_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS emd_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS above_below_percentage DECIMAL(10,4),
ADD COLUMN IF NOT EXISTS ref_person VARCHAR(255),
ADD COLUMN IF NOT EXISTS work_awarded_status VARCHAR(255),
ADD COLUMN IF NOT EXISTS time_limit VARCHAR(255),
ADD COLUMN IF NOT EXISTS sd_fdr_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS remarks TEXT,
ADD COLUMN IF NOT EXISTS sd_rab_deduction DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS sd_rab_return_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS additional_deduction VARCHAR(255),
ADD COLUMN IF NOT EXISTS work_completed_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS pending_work_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS dlp_ended_on DATE,
ADD COLUMN IF NOT EXISTS emd_return_status VARCHAR(255),
ADD COLUMN IF NOT EXISTS sd_return_status VARCHAR(255),
ADD COLUMN IF NOT EXISTS sd_rm_rab_return_status VARCHAR(255),
ADD COLUMN IF NOT EXISTS status VARCHAR(255);
