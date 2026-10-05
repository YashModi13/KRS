-- Migration v28: Increase VARCHAR column lengths to VARCHAR(2000) in project_locations, projects, and bulk_upload_projects_data tables

ALTER TABLE krs_schema.project_locations ALTER COLUMN village_name TYPE VARCHAR(2000);
ALTER TABLE krs_schema.project_locations ALTER COLUMN school_name TYPE VARCHAR(2000);
ALTER TABLE krs_schema.project_locations ALTER COLUMN block TYPE VARCHAR(2000);
ALTER TABLE krs_schema.project_locations ALTER COLUMN head TYPE VARCHAR(2000);
ALTER TABLE krs_schema.project_locations ALTER COLUMN repairing TYPE VARCHAR(2000);
ALTER TABLE krs_schema.project_locations ALTER COLUMN time_limit TYPE VARCHAR(2000);

ALTER TABLE krs_schema.projects ALTER COLUMN notice_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN package_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN time_limit TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN work_order_number TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN department_name_id TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN additional_deduction TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN tender_fee_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.projects ALTER COLUMN emd_no TYPE VARCHAR(2000);

ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN notice_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN package_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN time_limit TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN work_order_number TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN department_name TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN additional_deduction TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN tender_fee_no TYPE VARCHAR(2000);
ALTER TABLE krs_schema.bulk_upload_projects_data ALTER COLUMN emd_no TYPE VARCHAR(2000);
