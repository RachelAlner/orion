CREATE TABLE schedules (
    id UUID PRIMARY KEY, 
    user_id UUID NOT NULL, 
    period_start TIMESTAMP NOT NULL, 
    period_end TIMESTAMP NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 

    CONSTRAINT fk_schedules_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE, 
    
    CONSTRAINT chk_schedules_period
        CHECK (period_start < period_end)
);

CREATE INDEX idx_schedules_user_id
    ON schedules(user_id);

CREATE TABLE schedule_blocks (
    id UUID PRIMARY KEY, 
    schedule_id UUID NOT NULL, 
    task_id UUID NOT NULL, 
    start_time TIMESTAMP NOT NULL, 
    end_time TIMESTAMP NOT NULL, 

    CONSTRAINT fk_schedule_blocks_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES schedules(id)
        ON DELETE CASCADE, 
    
    CONSTRAINT fk_schedule_blocks_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE, 
    
    CONSTRAINT chk_schedule_blocks_time
        CHECK (start_time < end_time)
);

CREATE INDEX idx_schedule_blocks_schedule_id
    ON schedule_blocks(schedule_id);

CREATE INDEX idx_schedule_blocks_task_id
    ON schedule_blocks(task_id);