CREATE TABLE user_skills
(
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    skill_id    BIGINT      NOT NULL REFERENCES skills (id),
    proficiency VARCHAR(20) NOT NULL
        CHECK (proficiency IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    PRIMARY KEY (user_id, skill_id)
);

-- migrate existing employee_skills data to user_skills
INSERT INTO user_skills (user_id, skill_id, proficiency)
SELECT e.user_id, es.skill_id, es.proficiency
FROM employee_skills es
         JOIN employees e ON e.id = es.employee_id;