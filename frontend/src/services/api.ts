import { getToken } from "./authStorage";

const API_BASE_URL = "http://localhost:8080";

export async function apiRequest<T>(
    path: string, 
    options: RequestInit = {}
): Promise<T> {
    const token = getToken();

    const headers = new Headers(options.headers);

    headers.set(
        "Content-Type",
        "application/json"
    );

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
        throw new Error(
            `Request failed with status ${response.status}`
        );
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json();
}