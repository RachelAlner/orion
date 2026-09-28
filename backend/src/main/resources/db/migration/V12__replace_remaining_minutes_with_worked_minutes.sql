ALTER TABLE tasks 
ADD COLUMN worked_minutes INTEGER NOT NULL DEFAULT 0;

UPDATE tasks 
SET worked_minutes = GREATEST(
    estimated_minutes - remaining_minutes, 
    0
);

ALTER TABLE tasks 
DROP COLUMN remaining_minutes;