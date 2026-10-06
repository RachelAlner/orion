import { apiRequest } from "./api";

import type { TaskDependency } from "../types/taskDependency";

export function getTaskDependencies(
    projectId: string,
    taskId: string
): Promise<TaskDependency[]> {
    return apiRequest<TaskDependency[]>(
        `/api/projects/${projectId}/tasks/${taskId}/dependencies`
    );
}

export function addTaskDependency(
    projectId: string, 
    taskId: string, 
    dependsOnTaskId: string
): Promise<TaskDependency> {
    return apiRequest<TaskDependency>(
        `/api/projects/${projectId}/tasks/${taskId}/dependencies`,
        {
            method: "POST",
            body: JSON.stringify({
                dependsOnTaskId,
            }),
        }
    );
}

export function deleteTaskDependency(
    projectId: string, 
    taskId: string,
    dependsOnTaskId: string
): Promise<void> {
    return apiRequest<void>(
        `/api/projects/${projectId}/tasks/${taskId}/dependencies/${dependsOnTaskId}`,
        {
            method: "DELETE",
        }
    );
}