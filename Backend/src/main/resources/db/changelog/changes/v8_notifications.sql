-- liquibase formatted sql

-- changeset Antigravity:18-create-notifications
-- validCheckSum: ANY
-- comment: Create notifications table for user alerts

CREATE TABLE IF NOT EXISTS krs_schema.notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES krs_schema.users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
