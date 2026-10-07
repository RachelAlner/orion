import { apiRequest } from "./api";
import type {
    Availability, 
    CreateAvailabilityRequest,
    UpdateAvailabilityRequest,
} from "../types/availability";

export function getAvailability(): Promise<Availability[]> {
    return apiRequest<Availability[]>(
        "/api/availability"
    );
}

export function createAvailability(
    request: CreateAvailabilityRequest
): Promise<Availability> {
    return apiRequest<Availability>(
        "/api/availability",
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}

export function updateAvailability(
    availabilityId: string, 
    request: UpdateAvailabilityRequest
): Promise<Availability> {
    return apiRequest<Availability>(
        `/api/availability/${availabilityId}`,
        {
            method: "PUT",
            body: JSON.stringify(request),
        }
    );
}

export function deleteAvailability(
    availabilityId: string
): Promise<void> {
    return apiRequest<void>(
        `/api/availability/${availabilityId}`,
        {
            method: "DELETE",
        }
    );
}