ALTER TABLE tasks 
    ALTER COLUMN estimated_minutes DROP NOT NULL,
    ALTER COLUMN priority DROP NOT NULL;

ALTER TABLE tasks 
    DROP CONSTRAINT chk_tasks_estimated_minutes,
    DROP CONSTRAINT chk_tasks_priority;

ALTER TABLE tasks 
    ADD CONSTRAINT chk_tasks_estimated_minutes
        CHECK (estimated_minutes IS NULL OR estimated_minutes > 0),
    ADD CONSTRAINT chk_tasks_priority
        CHECK (priority IS NULL OR priority BETWEEN 1 AND 5);