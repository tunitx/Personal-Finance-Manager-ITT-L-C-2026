ALTER TABLE users
    ADD COLUMN full_name    VARCHAR(100),
    ADD COLUMN department   VARCHAR(100),
    ADD COLUMN designation  VARCHAR(100),
    ADD COLUMN status       VARCHAR(20) DEFAULT 'BENCH'
                            CHECK (status IN ('BENCH', 'ALLOCATED'));