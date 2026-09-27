import { apiRequest } from "./api";
import type {
    CreateProjectRequest, 
    Project,
    UpdateProjectRequest,
} from "../types/project";

export function getProjects(): Promise<Project[]> {
    return apiRequest<Project[]>(
        "/api/projects"
    );
}

export function getProject(
    projectId: string
): Promise<Project> {
    return apiRequest<Project>(
        `/api/projects/${projectId}`
    );
}

export function createProject(
    request: CreateProjectRequest
): Promise<Project> {
    return apiRequest<Project>(
        "/api/projects",
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}

export function updateProject(
    projectId: string,
    request: UpdateProjectRequest
): Promise<Project>{
    return apiRequest<Project>(
        `/api/projects/${projectId}`,
        {
            method: "PUT",
            body: JSON.stringify(request),
        }
    );
}

export function deleteProject(
    projectId: string
): Promise<void> {
    return apiRequest<void>(
        `/api/projects/${projectId}`,
        {
            method: "DELETE",
        }
    );
}