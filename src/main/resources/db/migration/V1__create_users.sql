CREATE TABLE users (
                       id                    BIGSERIAL          PRIMARY KEY,
                       username              VARCHAR(50)     NOT NULL UNIQUE,
                       email                 VARCHAR(100)    NOT NULL UNIQUE,
                       password_hash         VARCHAR(255)    NOT NULL,
                       role                  VARCHAR(20)     NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
                       force_password_change BOOLEAN         NOT NULL DEFAULT TRUE,
                       is_active             BOOLEAN         NOT NULL DEFAULT TRUE,
                       created_at            TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at            TIMESTAMP       NOT NULL DEFAULT NOW()
);