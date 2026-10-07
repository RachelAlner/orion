import { apiRequest } from "./api";
import type { ScheduleResponse } from "../types/schedule";

export function generateSchedule(
    periodStart: string, 
    periodEnd: string
): Promise<ScheduleResponse> {
    return apiRequest<ScheduleResponse>(
        "/api/schedules/generate",
        {
            method: "POST",
            body: JSON.stringify({
                periodStart,
                periodEnd,
            }),
        }
    );
}

export function getCurrentSchedule(): Promise<ScheduleResponse> {
    return apiRequest<ScheduleResponse>(
        "/api/schedules/current"
    );
}