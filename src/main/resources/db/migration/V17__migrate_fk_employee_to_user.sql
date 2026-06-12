-- allocations: rename employee_id to user_id
ALTER TABLE allocations RENAME COLUMN employee_id TO user_id;

-- timesheets: rename employee_id to user_id
ALTER TABLE timesheets RENAME COLUMN employee_id TO user_id;

-- projects: drop old FK and recreate pointing to users
ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_manager_id_fkey;
ALTER TABLE projects ADD CONSTRAINT projects_manager_id_fkey
    FOREIGN KEY (manager_id) REFERENCES users(id);

-- allocations: drop old FK (still named with employee_id) and recreate
ALTER TABLE allocations DROP CONSTRAINT IF EXISTS allocations_employee_id_fkey;
ALTER TABLE allocations ADD CONSTRAINT allocations_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id);

-- timesheets: drop old FK and recreate
ALTER TABLE timesheets DROP CONSTRAINT IF EXISTS timesheets_employee_id_fkey;
ALTER TABLE timesheets ADD CONSTRAINT timesheets_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id);