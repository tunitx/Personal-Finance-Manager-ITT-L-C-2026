INSERT INTO users (username, email, password_hash, role, force_password_change, is_active)
VALUES (
           'admin',
           'admin@techserve.com',
           '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
           'ADMIN',
           TRUE,
           TRUE
       );

INSERT INTO system_config (llm_provider, scheduler_interval_hrs, max_weekly_hours)
VALUES ('GEMINI', 4, 40);