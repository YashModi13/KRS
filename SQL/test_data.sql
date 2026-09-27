-- =========================================================================
-- KRS Construction - TEST DATA SCRIPT
-- =========================================================================
-- Instructions: Run this script directly on your PostgreSQL database
-- (e.g., using pgAdmin or psql) to instantly populate test data.
-- Ensure you select the 'krs_db' database before running!
-- =========================================================================

-- Disable foreign key constraints temporarily just in case of re-runs
SET session_replication_role = 'replica';

-- 1. Clear existing test data (optional, remove if you want to keep appending)
DELETE FROM krs_schema.notifications;
DELETE FROM krs_schema.daily_tasks;
DELETE FROM krs_schema.approvals;
DELETE FROM krs_schema.ra_bills;
DELETE FROM krs_schema.project_locations;
DELETE FROM krs_schema.projects;

-- 2. Insert Projects (Tender Details)
INSERT INTO krs_schema.projects (
    id, department_name, package_no, tender_id, name_of_work, estimated_tender_cost, tendered_cost, 
    work_order_date, defects_liability_period, security_deposit_date, created_by
) VALUES 
(1, 'Education Department Gujarat', 'PKG-2026-A1', 'TND-2026-9901', 'Construction of 4 New Classrooms and MDM Shed at Ahmedabad Block', 4500000.00, 4400000.00, '2026-01-15', '12 Months', '2026-01-10', 'admin'),
(2, 'Road & Building Dept', 'PKG-2026-B2', 'TND-2026-8802', 'Repairing of Primary School and CWSN Toilet in Surat District', 2500000.00, 2450000.00, '2026-03-01', '6 Months', '2026-02-28', 'admin'),
(3, 'Education Department Gujarat', 'PKG-2026-C3', 'TND-2026-7703', 'Construction of Library and Science Lab in Vadodara', 8000000.00, 7850000.00, '2026-08-10', '24 Months', '2026-08-05', 'admin');

-- 3. Insert Project Locations (Schools)
INSERT INTO krs_schema.project_locations (project_id, block, school_id, school_name, head, new_acr, shed) VALUES 
(1, 'Ahmedabad City', 'SCH-101', 'Saraswati Vidyalaya', 'Primary', '4 Classrooms', '1 MDM Shed'),
(1, 'Ahmedabad Rural', 'SCH-102', 'Bopal Prathamik Shala', 'Secondary', '2 Classrooms', 'None');

INSERT INTO krs_schema.project_locations (project_id, block, school_id, school_name, head, repairing, cwsn_toilet) VALUES 
(2, 'Surat Central', 'SCH-201', 'Kanya Vidhyalay', 'Primary', 'Full Roof Repair', '2 Toilets');

-- 4. Insert RA Bills Status
INSERT INTO krs_schema.ra_bills (
    project_id, ra_bill_number, name_of_agency, date_of_bill, gross_bill_amount, 
    cgst_9_percent, sgst_9_percent, rm_5_percent, tds_2_percent, net_payment, pre_audit_remarks, invoice_submitted
) VALUES 
(1, '1st RA Bill', 'KRS Construction', '2026-04-10', 1000000.00, 90000.00, 90000.00, 50000.00, 20000.00, 1010000.00, 'First phase completed smoothly.', TRUE),
(1, '2nd RA Bill', 'KRS Construction', '2026-07-25', 1500000.00, 135000.00, 135000.00, 75000.00, 30000.00, 1515000.00, 'Second phase concrete pouring done.', TRUE),
(2, '1st RA Bill', 'KRS Construction', '2026-05-15', 800000.00, 72000.00, 72000.00, 40000.00, 16000.00, 808000.00, 'Roof repair materials procured.', FALSE);

-- 5. Insert Daily Tasks & Project Status
INSERT INTO krs_schema.daily_tasks (project_id, task_description, assigned_to, status, remarks) VALUES 
(1, 'Concrete pouring for block A foundation', 'Raj Patel', TRUE, 'Completed on time despite rain.'),
(1, 'Steel binding for columns', 'Amit Shah', FALSE, 'Pending steel delivery.'),
(2, 'Site Inspection and plumbing layout', 'Sunil Verma', TRUE, 'Approved by chief engineer.'),
(3, 'Initial site clearing and leveling', 'Raj Patel', FALSE, 'Starting next Monday.');

-- 6. Insert Approvals / Manjuri
INSERT INTO krs_schema.approvals (project_id, approval_type, amount, time_limit_extension_date) VALUES 
(1, 'Time Extension', NULL, '2026-10-15'),
(2, 'Budget Revision', 150000.00, NULL);

-- 7. Insert Notifications for Super Admin (user_id = 1)
INSERT INTO krs_schema.notifications (user_id, message, type, is_read) VALUES 
(1, 'Project PKG-2026-A1 is approaching its time limit. (45 days left)', 'TIME_LIMIT', FALSE),
(1, '2nd RA Bill for Saraswati Vidyalaya requires your final pre-audit signature.', 'ALERT', FALSE),
(1, 'Retention Money (RM) release is pending for Project PKG-2026-B2.', 'RM_RELEASE', FALSE),
(1, 'Raj Patel marked "Concrete pouring" as completed.', 'TASK_UPDATE', TRUE);

-- Reset sequence to ensure future inserts from application don't collide with hardcoded IDs
SELECT setval('krs_schema.projects_id_seq', (SELECT MAX(id) FROM krs_schema.projects));
SELECT setval('krs_schema.project_locations_id_seq', (SELECT MAX(id) FROM krs_schema.project_locations));
SELECT setval('krs_schema.ra_bills_id_seq', (SELECT MAX(id) FROM krs_schema.ra_bills));

-- Re-enable constraints
SET session_replication_role = 'origin';

-- =========================================================================
-- DATA INSERTION COMPLETE
-- =========================================================================
