ALTER TABLE users ADD COLUMN role_id BIGINT REFERENCES roles(id);

UPDATE users u
SET role_id = r.id
    FROM roles r
WHERE r.name = u.role;

ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE users DROP COLUMN role;