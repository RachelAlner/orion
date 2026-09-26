ALTER TABLE tasks
    ADD COLUMN remaining_minutes INTEGER;

UPDATE tasks
SET remaining_minutes = estimated_minutes;

ALTER TABLE tasks 
    ALTER COLUMN remaining_minutes SET NOT NULL;