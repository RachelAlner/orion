import { apiRequest } from "./api";
import type {
    CreateTaskRequest,
    Task,
    UpdateTaskRequest,
    UpdateTaskProgressRequest,
} from "../types/task";

export function getTasks(
    projectId: string
): Promise<Task[]> {
    return apiRequest<Task[]>(
        `/api/projects/${projectId}/tasks`
    );
}

export function getTask(
    projectId: string, 
    taskId: string
): Promise<Task> {
    return apiRequest<Task>(
        `/api/projects/${projectId}/tasks/${taskId}`
    );
}

export function createTask(
    projectId: string, 
    request: CreateTaskRequest
): Promise<Task> {
    return apiRequest<Task>(
        `/api/projects/${projectId}/tasks`,
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}

export function updateTask(
    projectId: string, 
    taskId: string, 
    request: UpdateTaskRequest
): Promise<Task> {
    return apiRequest<Task>(
        `/api/projects/${projectId}/tasks/${taskId}`,
        {
            method: "PUT",
            body: JSON.stringify(request),
        }
    );
}

export function deleteTask(
    projectId: string, 
    taskId: string
): Promise<void> {
    return apiRequest<void>(
        `/api/projects/${projectId}/tasks/${taskId}`,
        {
            method: "DELETE",
        }
    );
}

export function updateTaskProgress(
    projectId: string,
    taskId: string, 
    request: UpdateTaskProgressRequest
): Promise<Task> {
    return apiRequest<Task>(
        `/api/projects/${projectId}/tasks/${taskId}/progress`,
        {
            method: "PATCH",
            body: JSON.stringify(request),
        }
    );
}

export function completeTask(
    projectId: string, 
    taskId: string
): Promise<Task> {
    return apiRequest<Task>(
        `/api/projects/${projectId}/tasks/${taskId}/complete`,
        {
            method: "POST",
        }
    );
}