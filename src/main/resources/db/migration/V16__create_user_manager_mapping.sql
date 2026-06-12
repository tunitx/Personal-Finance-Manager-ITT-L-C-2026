CREATE TABLE user_manager_mapping
(
    user_id    BIGINT NOT NULL REFERENCES users (id),
    manager_id BIGINT NOT NULL REFERENCES users (id),
    PRIMARY KEY (user_id)
);