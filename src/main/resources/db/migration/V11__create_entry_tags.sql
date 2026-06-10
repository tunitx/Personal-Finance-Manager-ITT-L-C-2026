CREATE TABLE entry_tags
(
    entry_id BIGINT NOT NULL REFERENCES timesheet_entries (id),
    tag_id   BIGINT NOT NULL REFERENCES activity_tags (id),
    PRIMARY KEY (entry_id, tag_id)
);