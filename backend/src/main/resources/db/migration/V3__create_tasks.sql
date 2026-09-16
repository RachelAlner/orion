CREATE TABLE tasks (
    id UUID PRIMARY KEY, 
    project_id UUID NOT NULL, 
    title VARCHAR(255) NOT NULL,
    description VARCHAR(20000),
    estimated_minutes INTEGER NOT NULL,
    deadline DATE,
    priority INTEGER NOT NULL, 
    status VARCHAR(50) NOT NULL, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    completed_at TIMESTAMP WITH TIME ZONE, 

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE, 
    
    CONSTRAINT chk_tasks_estimated_minutes
        CHECK (estimated_minutes > 0), 
    
    CONSTRAINT chk_tasks_priority 
        CHECK (priority BETWEEN 1 AND 5)
);

CREATE INDEX idx_tasks_project_id
    ON tasks(project_id);