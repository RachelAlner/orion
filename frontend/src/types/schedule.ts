export interface ScheduleBlock {
    taskId: string;
    startTime: string;
    endTime: string;
}

export type UnscheduledReason = 
    | "INSUFFICIENT_AVAILABILITY"
    | "DEPENDENCY_BLOCKED"
    | "DEADLINE_UNACHIEVABLE";

export interface UnscheduledTask {
    taskId: string;
    remainingMinutes: number; 
    reason: UnscheduledReason;
}

export interface ScheduleResponse {
    periodStart: string;
    periodEnd: string;
    scheduledBlocks: ScheduleBlock[];
    unscheduledTasks: UnscheduledTask[];
}