-- liquibase formatted sql

-- changeset Antigravity:14-create-domain-tables
-- validCheckSum: ANY
-- comment: Create core tables based on requirement.pdf flow for KRS Construction PM

DROP TABLE IF EXISTS krs_schema.ra_bills CASCADE;
DROP TABLE IF EXISTS krs_schema.constructions CASCADE;
DROP TABLE IF EXISTS krs_schema.tenders CASCADE;

CREATE TABLE IF NOT EXISTS krs_schema.projects (
    id BIGSERIAL PRIMARY KEY,
    work_order_number VARCHAR(255),
    negotiation_letter_file VARCHAR(255),
    village_name VARCHAR(500),
    security_deposit_amount DECIMAL(15,2),
    security_deposit_type VARCHAR(100),
    security_deposit_file VARCHAR(255),
    retention_money_per_bill DECIMAL(15,2),
    extra_excess_amount DECIMAL(15,2),
    time_limit_extension DATE,
    completion_date_actual DATE,
    completion_date_extended DATE,
    letter_by_krs_file VARCHAR(255),
    letter_by_dept_file VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS krs_schema.ra_bills (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES krs_schema.projects(id),
    total_ra_bill_amount DECIMAL(15,2),
    invoice_submitted BOOLEAN DEFAULT FALSE,
    bill_check_person_name VARCHAR(255),
    dept_ra_bill_copy_file VARCHAR(255),
    bill_deposited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS krs_schema.approvals (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES krs_schema.projects(id),
    approval_type VARCHAR(100),
    amount DECIMAL(15,2),
    time_limit_extension_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS krs_schema.daily_tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES krs_schema.projects(id),
    task_description TEXT,
    assigned_to VARCHAR(255),
    status BOOLEAN DEFAULT FALSE,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
