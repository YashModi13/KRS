-- liquibase formatted sql

-- changeset Antigravity:17-update-admin-credentials
-- validCheckSum: ANY
-- comment: Update admin username and password for YashModi

UPDATE krs_schema.users 
SET username = 'YashModi1310Admin', 
    password = '$2b$10$qJUjzLIGtOcy66B1trtnWe9cV2CRDGWg3HPfzWnfWrdTibDlXzWWa'
WHERE id = 1;
