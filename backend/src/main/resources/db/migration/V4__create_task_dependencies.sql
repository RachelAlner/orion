CREATE TABLE task_dependencies (
    task_id UUID NOT NULL,
    depends_on_task_id UUID NOT NULL, 

    CONSTRAINT pk_task_dependencies
        PRIMARY KEY (task_id, depends_on_task_id), 
    
    CONSTRAINT fk_task_dependencies_task
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_task_dependencies_depends_on
        FOREIGN KEY (depends_on_task_id)
        REFERENCES  tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_task_dependencies_not_self
        CHECK (task_id <> depends_on_task_id)
);

CREATE INDEX idx_task_dependencies_depends_on
    ON task_dependencies(depends_on_task_id);

