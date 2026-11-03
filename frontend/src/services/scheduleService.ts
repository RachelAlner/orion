import { apiRequest } from "./api";
import type { ScheduleResponse } from "../types/schedule";

export function generateSchedule(): Promise<ScheduleResponse> {
    return apiRequest<ScheduleResponse>(
        "/api/schedules/generate",
        {
            method: "POST",
        }
    );
}

export function getCurrentSchedule(): Promise<ScheduleResponse> {
    return apiRequest<ScheduleResponse>(
        "/api/schedules/current"
    );
}