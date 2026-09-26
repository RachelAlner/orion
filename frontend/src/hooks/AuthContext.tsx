import {
    createContext,
    useContext,
    useState,
    type ReactNode,
} from "react";

import {
    clearToken, 
    getToken,
    setToken,
} from "../services/authStorage";

interface AuthContextValue {
    token: string | null;
    isAuthenticated: boolean;
    loginWithToken: (token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(
    undefined
);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({
    children,
}: AuthProviderProps) {
    const [token, setAuthToken] = useState<string | null>(
        getToken()
    );

    function loginWithToken(newToken: string) {
        setToken(newToken);
        setAuthToken(newToken);
    }

    function logout() {
        clearToken();
        setAuthToken(null);
    }

    return (
        <AuthContext.Provider
            value={{
                token, 
                isAuthenticated: token !== null,
                loginWithToken,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if(!context) {
        throw new Error(
            "useAuth must be used within AuthProvider"
        );
    }

    return context;
}