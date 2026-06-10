CREATE TABLE system_config (
                               id                      BIGSERIAL        PRIMARY KEY,
                               llm_provider           VARCHAR(50)  NOT NULL DEFAULT 'GEMINI',
                               llm_api_key_enc        VARCHAR(500),
                               scheduler_interval_hrs INTEGER      NOT NULL DEFAULT 4,
                               max_weekly_hours       INTEGER      NOT NULL DEFAULT 40,
                               updated_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);