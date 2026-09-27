-- liquibase formatted sql

-- changeset YashModi:13-update-superadmin-password-fix
-- comment: Update superadmin dummy password to a verified BCrypt hash for 'admin123'
UPDATE krs_schema.users SET password = '$2a$10$K5fN2N2taUClLjfZE4pJgu7iJMh14xpU/EQs9Nx9Xx9MosB6aUQVm' WHERE username = 'superadmin';
