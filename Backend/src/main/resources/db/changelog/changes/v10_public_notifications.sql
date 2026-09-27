-- liquibase formatted sql
-- changeset Antigravity:10-public-notifications

-- Drop user-specific columns from notifications table to make it public
ALTER TABLE krs_schema.notifications DROP COLUMN IF EXISTS user_id;
ALTER TABLE krs_schema.notifications DROP COLUMN IF EXISTS is_read;

-- Create mapping table to track which user read which notification
CREATE TABLE IF NOT EXISTS krs_schema.user_notification_reads (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES krs_schema.users(id) ON DELETE CASCADE,
    notification_id BIGINT NOT NULL REFERENCES krs_schema.notifications(id) ON DELETE CASCADE,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, notification_id)
);
