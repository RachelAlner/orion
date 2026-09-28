export type TaskStatus = 
    | "TODO"
    | "IN_PROGRESS"
    | "COMPLETED"
    | "CANCELLED";

export interface Task {
    id: string;
    projectId: string;
    title: string;
    description: string | null;
    estimatedMinutes: number | null;
    remainingMinutes: number | null;
    deadline: string | null;
    priority: number | null;
    status: TaskStatus;
    createdAt: string;
    updatedAt: string;
    completedAt: string | null;
}

export interface CreateTaskRequest {
    title: string;
    description: string | null;
    estimatedMinutes: number | null;
    deadline: string | null;
    priority: number | null;
}

export interface UpdateTaskRequest {
    title: string;
    description: string | null;
    estimatedMinutes: number | null;
    deadline: string | null;
    priority: number | null;
}