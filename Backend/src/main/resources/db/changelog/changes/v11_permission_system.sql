-- liquibase formatted sql
-- changeset Yash Modi:11-permission-system

CREATE TABLE krs_schema.page_master (
    id BIGSERIAL PRIMARY KEY,
    page_name VARCHAR(255) NOT NULL,
    route_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE krs_schema.action_master (
    id BIGSERIAL PRIMARY KEY,
    action_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE krs_schema.page_action_mapping (
    id BIGSERIAL PRIMARY KEY,
    page_id BIGINT NOT NULL REFERENCES krs_schema.page_master(id),
    action_id BIGINT NOT NULL REFERENCES krs_schema.action_master(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE krs_schema.role_page_action_mapping (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES krs_schema.roles(id),
    page_action_id BIGINT NOT NULL REFERENCES krs_schema.page_action_mapping(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE TABLE krs_schema.user_page_action_mapping (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES krs_schema.users(id),
    page_action_id BIGINT NOT NULL REFERENCES krs_schema.page_action_mapping(id),
    is_allowed BOOLEAN NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);
