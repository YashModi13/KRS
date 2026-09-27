-- liquibase formatted sql

-- changeset YashModi:1-users-table
-- comment: Initial setup of users table
CREATE TABLE krs_schema.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset YashModi:2-roles-table
-- comment: Initial setup of roles table
CREATE TABLE krs_schema.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- changeset YashModi:3-user-roles-table
-- comment: Mapping table for user and roles
CREATE TABLE krs_schema.user_roles (
    user_id INTEGER REFERENCES krs_schema.users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES krs_schema.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- changeset YashModi:4-permissions-table
-- comment: Initial setup of permissions table
CREATE TABLE krs_schema.permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- changeset YashModi:5-role-permissions-table
-- comment: Mapping table for role and permissions
CREATE TABLE krs_schema.role_permissions (
    role_id INTEGER REFERENCES krs_schema.roles(id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES krs_schema.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- changeset YashModi:6-tenders-table
-- comment: Initial setup of tenders table for KRS Construction
CREATE TABLE krs_schema.tenders (
    id SERIAL PRIMARY KEY,
    tender_no VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tender_amount DECIMAL(15,2),
    agreement_amount DECIMAL(15,2),
    status VARCHAR(50) DEFAULT 'Running',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- changeset YashModi:7-constructions-table
-- comment: Constructions mapping back to single tender
CREATE TABLE krs_schema.constructions (
    id SERIAL PRIMARY KEY,
    tender_id INTEGER REFERENCES krs_schema.tenders(id) ON DELETE CASCADE,
    type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    village VARCHAR(100),
    taluka VARCHAR(100),
    district VARCHAR(100),
    physical_progress DECIMAL(5,2) DEFAULT 0,
    financial_progress DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'Running'
);

-- changeset YashModi:8-rabills-table
-- comment: RA bills tracking mapped to tender and construction
CREATE TABLE krs_schema.ra_bills (
    id SERIAL PRIMARY KEY,
    tender_id INTEGER REFERENCES krs_schema.tenders(id) ON DELETE CASCADE,
    construction_id INTEGER REFERENCES krs_schema.constructions(id) ON DELETE CASCADE,
    bill_no VARCHAR(100) NOT NULL,
    submitted_date DATE,
    amount DECIMAL(15,2) NOT NULL,
    gst_amount DECIMAL(15,2) DEFAULT 0,
    withheld_amount DECIMAL(15,2) DEFAULT 0,
    passed_date DATE,
    paid_date DATE,
    status VARCHAR(50) DEFAULT 'Pending Approval',
    remarks TEXT
);

-- changeset YashModi:9-indexes
-- comment: Add indexes for better performance
CREATE INDEX idx_tenders_tender_no ON krs_schema.tenders(tender_no);
CREATE INDEX idx_constructions_tender_id ON krs_schema.constructions(tender_id);
CREATE INDEX idx_ra_bills_tender_id ON krs_schema.ra_bills(tender_id);
CREATE INDEX idx_ra_bills_construction_id ON krs_schema.ra_bills(construction_id);
CREATE INDEX idx_user_roles_user_id ON krs_schema.user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON krs_schema.user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON krs_schema.role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON krs_schema.role_permissions(permission_id);

-- changeset YashModi:10-audit-logs-table
-- comment: Audit logs for tracking user actions
CREATE TABLE krs_schema.audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL, -- e.g. CREATE, UPDATE, DELETE
    entity_name VARCHAR(100) NOT NULL, -- e.g. TENDER, CONSTRUCTION
    entity_id VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    performed_by VARCHAR(50),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_entity ON krs_schema.audit_logs(entity_name, entity_id);

-- changeset YashModi:11-seed-initial-data
-- comment: Seeding super admin role, user and initial permissions
INSERT INTO krs_schema.roles (name, description) VALUES ('SUPER_ADMIN', 'Super Administrator with all permissions');
INSERT INTO krs_schema.roles (name, description) VALUES ('ADMIN', 'Administrator');
INSERT INTO krs_schema.roles (name, description) VALUES ('USER', 'Standard User');

INSERT INTO krs_schema.permissions (name, description) VALUES ('VIEW_TENDERS', 'Can view tenders');
INSERT INTO krs_schema.permissions (name, description) VALUES ('MANAGE_TENDERS', 'Can create/edit/delete tenders');
INSERT INTO krs_schema.permissions (name, description) VALUES ('VIEW_CONSTRUCTIONS', 'Can view constructions');
INSERT INTO krs_schema.permissions (name, description) VALUES ('MANAGE_CONSTRUCTIONS', 'Can create/edit/delete constructions');
INSERT INTO krs_schema.permissions (name, description) VALUES ('MANAGE_USERS', 'Can manage users and roles');
INSERT INTO krs_schema.permissions (name, description) VALUES ('MANAGE_ROLES', 'Can manage roles and permissions');

INSERT INTO krs_schema.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM krs_schema.roles r, krs_schema.permissions p WHERE r.name = 'SUPER_ADMIN';

INSERT INTO krs_schema.users (username, password, email) VALUES ('superadmin', '$2a$10$X/X5P5Q5g5.5/X5P5Q5g5.5/X5P5Q5g5.5/X5P5Q5g5.5/X5P5Q5g5.', 'admin@krs.com');
INSERT INTO krs_schema.user_roles (user_id, role_id)
SELECT u.id, r.id FROM krs_schema.users u, krs_schema.roles r WHERE u.username = 'superadmin' AND r.name = 'SUPER_ADMIN';
