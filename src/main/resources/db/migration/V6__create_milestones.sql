CREATE TABLE milestones
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT       NOT NULL REFERENCES projects (id),
    title      VARCHAR(100) NOT NULL,
    due_date   DATE         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED'
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'DONE', 'OVERDUE')),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);