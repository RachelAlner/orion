CREATE TABLE availability (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL, 
    end_time TIME NOT NULL,

    CONSTRAINT fk_availability_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE, 
    
    CONSTRAINT chk_availability_day_of_week
        CHECK (day_of_week BETWEEN 1 AND 7),
    
    CONSTRAINT chk_availability_time_range
        CHECK (start_time < end_time)
);

CREATE INDEX idx_availability_user_day
    ON availability(user_id, day_of_week);