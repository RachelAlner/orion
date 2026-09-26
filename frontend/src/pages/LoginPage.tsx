import { useState } from "react";
import { Link } from "react-router-dom";

import { login } from "../services/authService";
import { useAuth } from "../hooks/AuthContext";
import { useNavigate  } from "react-router-dom";

export default function LoginPage() {
    const navigate = useNavigate();
    const { loginWithToken } = useAuth();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = 
        useState(false);
    
    async function handleSubmit(
        event: React.SubmitEvent
    ) {
        event.preventDefault();

        setError("");
        setIsSubmitting(true);

        try {
            const response = await login(
                email,
                password
            );

            loginWithToken(response.token);

            navigate("/dashboard");
        } catch {
            setError(
                "Unable to log in. Please check your email and password."
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <main>
            <h1>Login</h1>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="email">
                        Email
                    </label>

                    <input 
                        id="email"
                        type="email"
                        value={email}
                        onChange={(event) =>
                            setEmail(event.target.value)
                        }
                        required
                    />
                </div>

                <div>
                    <label htmlFor="password">
                        Password
                    </label>

                    <input 
                        id="password"
                        type="password"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                        required
                    />
                </div>

                {error && (
                    <p role="alert">
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting
                        ? "Logging in..."
                        : "Log in"
                    }
                </button>
            </form>

            <p>
                Don't have an account?{" "}
                <Link to="/register">
                    Create an account
                </Link>
            </p>
        </main>
    );
}