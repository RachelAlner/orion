import { apiRequest } from "./api";
import type { AuthResponse } from "../types/auth";

interface LoginRequest {
    email: string;
    password: string;
}

interface RegisterRequest {
    email: string;
    password: string;
}

export function login(
    email: string, 
    password: string
): Promise<AuthResponse> {
    return apiRequest<AuthResponse>(
        "/api/auth/login",
        {
            method: "POST",
            body: JSON.stringify({
                email,
                password,
            } satisfies LoginRequest),
        }
    );
}

export function register(
    email: string,
    password: string
): Promise<AuthResponse> {
    return apiRequest<AuthResponse>(
        "/api/auth/register",
        {
            method: "POST",
            body: JSON.stringify({
                email, 
                password,
            } satisfies RegisterRequest),
        }
    );
}