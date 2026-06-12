-- V21__add_timesheet_freeze_columns.sql
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS timesheet_frozen      BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS reminder_count        INT     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_reminder_sent_at DATE;
