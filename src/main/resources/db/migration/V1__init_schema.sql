CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'TO_DO',
    priority VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_priority ON task(priority);