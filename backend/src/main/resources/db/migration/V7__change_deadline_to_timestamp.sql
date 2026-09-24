-- Change deadline from DATE to TIMESTAMP for projects and tasks
ALTER TABLE projects 
    ALTER COLUMN deadline TYPE TIMESTAMP;

ALTER TABLE tasks 
    ALTER COLUMN deadline TYPE TIMESTAMP;
