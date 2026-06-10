CREATE TABLE skills (
                        id         BIGSERIAL          PRIMARY KEY,
                        name      VARCHAR(100)    NOT NULL UNIQUE,
                        category  VARCHAR(20)     NOT NULL
                            CHECK (category IN ('BACKEND','FRONTEND','DEVOPS','QA','OTHER'))
);