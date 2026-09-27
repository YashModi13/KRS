-- liquibase formatted sql

-- changeset YashModi:12-update-superadmin-password
-- comment: Update superadmin dummy password to 'admin123'
UPDATE krs_schema.users SET password = '$2a$10$c.L45h3B9.R52u3z0374eOc3t.iBpswE4jZ02hLz5p7W.w20V08.a' WHERE username = 'superadmin';
