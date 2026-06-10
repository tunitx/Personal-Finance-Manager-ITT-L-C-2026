CREATE TABLE allocations
(
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT    NOT NULL REFERENCES employees (id),
    project_id      BIGINT    NOT NULL REFERENCES projects (id),
    utilisation_pct INTEGER   NOT NULL CHECK (utilisation_pct BETWEEN 1 AND 100),
    from_date       DATE      NOT NULL,
    to_date         DATE      NOT NULL,
    is_active       BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);