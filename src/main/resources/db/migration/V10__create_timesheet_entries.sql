CREATE TABLE timesheet_entries
(
    id           BIGSERIAL PRIMARY KEY,
    timesheet_id BIGINT    NOT NULL REFERENCES timesheets (id),
    project_id   BIGINT    NOT NULL REFERENCES projects (id),
    hours_logged INTEGER   NOT NULL CHECK (hours_logged > 0),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);