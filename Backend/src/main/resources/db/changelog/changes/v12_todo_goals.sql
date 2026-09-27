-- liquibase formatted sql
-- changeset yashmodi:12

CREATE TABLE krs_schema.todo_goals (
    id BIGSERIAL PRIMARY KEY,
    task_description TEXT NOT NULL,
    priority VARCHAR(50) NOT NULL CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    target_date DATE,
    is_done BOOLEAN DEFAULT FALSE,
    done_note TEXT DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    done_date TIMESTAMP,
    done_by BIGINT,
    
    CONSTRAINT fk_todo_created_by FOREIGN KEY (created_by) REFERENCES krs_schema.users(id),
    CONSTRAINT fk_todo_updated_by FOREIGN KEY (updated_by) REFERENCES krs_schema.users(id),
    CONSTRAINT fk_todo_done_by FOREIGN KEY (done_by) REFERENCES krs_schema.users(id)
);
