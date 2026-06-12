-- V22__add_email_config.sql
ALTER TABLE system_config
    ADD COLUMN IF NOT EXISTS smtp_from_email VARCHAR(255) DEFAULT 'noreply@techserve.com',
    ADD COLUMN IF NOT EXISTS smtp_from_name  VARCHAR(100) DEFAULT 'PRM System';
