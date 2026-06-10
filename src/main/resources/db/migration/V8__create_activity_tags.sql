CREATE TABLE activity_tags (
                               id     BIGSERIAL           PRIMARY KEY,
                               name  VARCHAR(100)    NOT NULL UNIQUE
);

INSERT INTO activity_tags (name) VALUES
                                     ('Backend API Development'),
                                     ('Microservices / Architecture'),
                                     ('Database Design & Queries'),
                                     ('WebSocket / Real-time Features'),
                                     ('Frontend Development'),
                                     ('Code Review / Mentoring'),
                                     ('Bug Fixing'),
                                     ('DevOps / Deployment'),
                                     ('Testing & QA'),
                                     ('Documentation');