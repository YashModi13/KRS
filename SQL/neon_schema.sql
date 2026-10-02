-- =========================================================================
-- KRS Construction - Neon Cloud Database Setup Script (For Manual DBeaver Run)
-- =========================================================================
-- NOTE: Neon automatically manages users and database creation.
-- This script creates the 'krs_schema' schema and Liquibase tracking tables.
-- =========================================================================

-- 1. Create the Schema for KRS Project
CREATE SCHEMA IF NOT EXISTS krs_schema;

-- 2. Set default search path to include krs_schema and public
SET search_path TO krs_schema, public;

-- 3. Create Liquibase Tracking Tables (Used by Liquibase auto-migrations)
CREATE TABLE IF NOT EXISTS krs_schema.databasechangeloglock (
    id INT NOT NULL,
    locked BOOLEAN NOT NULL,
    lockgranted TIMESTAMP WITHOUT TIME ZONE,
    lockedby VARCHAR(255),
    CONSTRAINT pk_databasechangeloglock PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS krs_schema.databasechangelog (
    id VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    dateexecuted TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    orderexecuted INT NOT NULL,
    exectype VARCHAR(10) NOT NULL,
    md5sum VARCHAR(35),
    description VARCHAR(255),
    comments VARCHAR(255),
    tag VARCHAR(255),
    liquibase VARCHAR(20),
    contexts VARCHAR(255),
    labels VARCHAR(255),
    deployment_id VARCHAR(10)
);

-- 4. Create Index for Liquibase Changelog
CREATE INDEX IF NOT EXISTS idx_databasechangelog_id_author_filename 
ON krs_schema.databasechangelog (id, author, filename);

-- =========================================================================
-- DEFAULT APPLICATION LOGIN CREDENTIALS
-- (Populated into users table when Spring Boot runs Liquibase)
-- =========================================================================
-- Username: YashModi1310Admin
-- Password: YaShAdmiN@1310
-- =========================================================================
