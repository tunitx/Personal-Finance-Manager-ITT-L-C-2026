CREATE TABLE projects
(
    id          BIGSERIAL PRIMARY KEY,
    manager_id  BIGINT       NOT NULL REFERENCES employees (id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL
        CHECK (status IN ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED')),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);