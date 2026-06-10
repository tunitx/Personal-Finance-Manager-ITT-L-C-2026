CREATE TABLE timesheets
(
    id           BIGSERIAL PRIMARY KEY,
    employee_id  BIGINT      NOT NULL REFERENCES employees (id),
    week_start   DATE        NOT NULL,
    status       VARCHAR(20) NOT NULL
        CHECK (status IN ('SUBMITTED', 'MISSED')),
    submitted_at TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_timesheet_emp_week UNIQUE (employee_id, week_start)
);