-- =========================================================================
-- KRS Construction - Update Admin User Credentials Query
-- =========================================================================
-- Target Credentials:
-- Username: YashModi
-- Password: Admin@1310
-- BCrypt Hash: $2a$10$d5hKRTOvAzJlWD1Ut4qkiuKeEG.giCmCm0nGoXfRvd4cIKfigRv8e
-- =========================================================================

-- 1. Update existing Admin User in public schema
UPDATE krs_schema.users 
SET username = 'YashModi', 
    password = '$2a$10$d5hKRTOvAzJlWD1Ut4qkiuKeEG.giCmCm0nGoXfRvd4cIKfigRv8e',
    is_active = true
WHERE username = 'YashModi1310Admin' OR id = 1;

-- 2. Update existing Admin User in krs_schema (if schema exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'krs_schema') THEN
        UPDATE krs_schema.users 
        SET username = 'YashModi', 
            password = '$2a$10$d5hKRTOvAzJlWD1Ut4qkiuKeEG.giCmCm0nGoXfRvd4cIKfigRv8e',
            is_active = true
        WHERE username = 'YashModi1310Admin' OR id = 1;
    END IF;
END $$;

-- 3. Upsert Admin User to guarantee user exists
INSERT INTO krs_schema.users (username, email, password, full_name, role, is_active)
VALUES ('YashModi', 'admin@krsconstruction.com', '$2a$10$d5hKRTOvAzJlWD1Ut4qkiuKeEG.giCmCm0nGoXfRvd4cIKfigRv8e', 'Yash Modi Admin', 'ROLE_ADMIN', true)
ON CONFLICT (username) 
DO UPDATE SET password = '$2a$10$d5hKRTOvAzJlWD1Ut4qkiuKeEG.giCmCm0nGoXfRvd4cIKfigRv8e', is_active = true;
