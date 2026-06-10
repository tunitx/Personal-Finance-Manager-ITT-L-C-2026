CREATE TABLE employees
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES users (id),
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    department  VARCHAR(100) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'BENCH'
        CHECK (status IN ('BENCH', 'ALLOCATED')),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);