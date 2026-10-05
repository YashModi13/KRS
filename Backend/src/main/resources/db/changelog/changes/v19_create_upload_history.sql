-- liquibase formatted sql

-- changeset Antigravity:29-create-project-upload-history
-- validCheckSum: ANY
-- comment: Create project_upload_history and bulk_upload_projects_data tables for bulk Excel import tracking

CREATE TABLE IF NOT EXISTS krs_schema.project_upload_history (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    uploaded_by VARCHAR(255) NOT NULL,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_rows INT NOT NULL DEFAULT 0,
    success_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    error_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS krs_schema.bulk_upload_projects_data (
    id BIGSERIAL PRIMARY KEY,
    master_id BIGINT REFERENCES krs_schema.project_upload_history(id) ON DELETE CASCADE,
    sr_no BIGINT,
    date_of_sub DATE,
    department_name VARCHAR(255),
    tender_id VARCHAR(255),
    notice_no VARCHAR(255),
    package_no VARCHAR(255),
    name_of_work TEXT,
    related_to VARCHAR(255),
    tender_fee DECIMAL(15,2),
    tender_fee_no VARCHAR(255),
    emd_amt DECIMAL(15,2),
    emd_no VARCHAR(255),
    estimated_tender_cost DECIMAL(15,2),
    tendered_cost DECIMAL(15,2),
    above_below_percentage DECIMAL(10,4),
    ref_person VARCHAR(255),
    work_awarded_status VARCHAR(255),
    work_order_number VARCHAR(255),
    work_order_date DATE,
    time_limit VARCHAR(255),
    security_deposit_amount DECIMAL(15,2),
    sd_fdr_no VARCHAR(255),
    remarks TEXT,
    sd_rab_deduction DECIMAL(15,2),
    sd_rab_return_amount DECIMAL(15,2),
    additional_deduction VARCHAR(255),
    work_completed_amount DECIMAL(15,2),
    pending_work_amount DECIMAL(15,2),
    completion_date_actual DATE,
    defects_liability_period VARCHAR(255),
    dlp_ended_on DATE,
    emd_return_status VARCHAR(255),
    sd_return_status VARCHAR(255),
    sd_rm_rab_return_status VARCHAR(255),
    status VARCHAR(255),
    is_failed BOOLEAN DEFAULT FALSE,
    failed_reason TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
