import { getToken } from "./authStorage";

const API_BASE_URL = "http://localhost:8080";

export class ApiError extends Error {
    status: number; 

    constructor(
        status: number,
        message: string
    ) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

export async function apiRequest<T>(
    path: string, 
    options: RequestInit = {}
): Promise<T> {
    const token = getToken();

    const headers = new Headers(options.headers);

    if (options.body) {
        headers.set(
            "Content-Type",
            "application/json"
        );
    }

    if (token) {
        headers.set(
            "Authorization",
            `Bearer ${token}`
        );
    }

    const response = await fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options, 
            headers,
        }
    );

    if (!response.ok) {
        const errorBody = await response.text();

        throw new ApiError(
            response.status,
            `Request failed with status ${response.status}: ${errorBody}`
        );
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}