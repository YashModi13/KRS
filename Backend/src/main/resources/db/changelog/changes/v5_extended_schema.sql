-- liquibase formatted sql

-- changeset Antigravity:15-extend-domain-tables
-- validCheckSum: ANY
-- comment: Extend projects and ra_bills tables based on Tender Details and RA Bill sheets

ALTER TABLE krs_schema.projects
ADD COLUMN IF NOT EXISTS department_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS date_of_sub DATE,
ADD COLUMN IF NOT EXISTS package_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS tender_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS name_of_work TEXT,
ADD COLUMN IF NOT EXISTS dd_no VARCHAR(255),
ADD COLUMN IF NOT EXISTS emd_amt DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS estimated_tender_cost DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS tendered_cost DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS work_order_date DATE,
ADD COLUMN IF NOT EXISTS defects_liability_period VARCHAR(255),
ADD COLUMN IF NOT EXISTS security_deposit_date DATE;

ALTER TABLE krs_schema.ra_bills
ADD COLUMN IF NOT EXISTS ra_bill_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS name_of_agency VARCHAR(255),
ADD COLUMN IF NOT EXISTS pan_number VARCHAR(100),
ADD COLUMN IF NOT EXISTS last_bill_paid_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS date_of_last_payment DATE,
ADD COLUMN IF NOT EXISTS amount_paid_upto_previous_bills DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS total_amount_of_current_ra_bill DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS total_amount_upto_this_bill DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS date_of_bill DATE,
ADD COLUMN IF NOT EXISTS gross_bill_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS cgst_9_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS sgst_9_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS total_gst DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS net_bill_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS rm_5_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS tds_2_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS labour_cess_1_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS cgst_1_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS sgst_1_percent DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS withheld_amount DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS liquidity_damage DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS net_payment DECIMAL(15,2),
ADD COLUMN IF NOT EXISTS pre_audit_remarks TEXT;
