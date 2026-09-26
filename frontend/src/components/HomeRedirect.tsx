import { Navigate } from "react-router-dom";

import { useAuth } from "../hooks/AuthContext";

export default function HomeRedirect() {
    const { isAuthenticated } = useAuth();

    return (
        <Navigate 
            to={
                isAuthenticated
                    ? "/dashboard"
                    : "/login"
            }
            replace
        />
    );
}