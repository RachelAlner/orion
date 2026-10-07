import type { DayOfWeek } from "./dayOfWeek";

export interface Availability {
    id: string;
    dayOfWeek: DayOfWeek;
    startTime: string;
    endTime: string;
}

export interface CreateAvailabilityRequest {
    dayOfWeek: DayOfWeek;
    startTime: string; 
    endTime: string;
}

export type UpdateAvailabilityRequest = 
    CreateAvailabilityRequest;