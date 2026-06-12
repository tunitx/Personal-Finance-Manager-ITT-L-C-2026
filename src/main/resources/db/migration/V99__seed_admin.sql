-- Seed admin user using role_id (FK) not role string column
INSERT INTO users (username, email, password_hash, role_id,
                   force_password_change, is_active)
SELECT 'admin',
       'admin@techserve.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVll3B8M1u',
       r.id,
       TRUE,
       TRUE
FROM roles r WHERE r.name = 'ADMIN'
    ON CONFLICT (username) DO NOTHING;

INSERT INTO system_config (llm_provider, llm_api_key_enc,
                           scheduler_interval_hrs, max_weekly_hours)
VALUES ('GROQ', NULL, 4, 40)
    ON CONFLICT DO NOTHING;