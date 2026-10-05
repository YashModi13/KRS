-- liquibase formatted sql

-- changeset Antigravity:35-add-city-village-state-to-department-master splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- comment: Add city_village and state columns to department_master with default state 'Gujarat'.

ALTER TABLE krs_schema.department_master 
ADD COLUMN IF NOT EXISTS city_village VARCHAR(255),
ADD COLUMN IF NOT EXISTS state VARCHAR(255) DEFAULT 'Gujarat';

-- Backfill state with 'Gujarat' where null
UPDATE krs_schema.department_master 
SET state = 'Gujarat' 
WHERE state IS NULL OR TRIM(state) = '';

-- Backfill city_village from name where name contains a comma
UPDATE krs_schema.department_master
SET city_village = INITCAP(TRIM(SUBSTRING(name FROM POSITION(',' IN name) + 1)))
WHERE (city_village IS NULL OR TRIM(city_village) = '')
  AND POSITION(',' IN name) > 0;
