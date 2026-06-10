CREATE TABLE employee_skills
(
    employee_id BIGINT      NOT NULL REFERENCES employees (id),
    skill_id    BIGINT      NOT NULL REFERENCES skills (id),
    proficiency VARCHAR(20) NOT NULL
        CHECK (proficiency IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    PRIMARY KEY (employee_id, skill_id)
);