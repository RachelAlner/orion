export type ProjectStatus = 
    | "ACTIVE"
    | "COMPLETED"
    | "ARCHIVED";

export interface Project {
    id: string;
    name: string;
    description: string | null;
    deadline: string | null;
    priority: number | null;
    status: ProjectStatus;
    createdAt: string; 
    updatedAt: string;
}

export interface CreateProjectRequest {
    name: string;
    description: string | null;
    deadline: string | null;
    priority: number | null;
}

export interface UpdateProjectRequest {
    name: string;
    description: string | null;
    deadline: string | null;
    priority: number | null;
}