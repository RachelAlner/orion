CREATE TABLE projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, 
    name VARCHAR(200) NOT NULL, 
    description TEXT, 
    deadline DATE, 
    priority INTEGER NOT NULL, 
    status VARCHAR(20) NOT NULL, 
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, 
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_projects_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE, 

    CONSTRAINT chk_projects_priority
        CHECK (priority BETWEEN 1 AND 5)
);