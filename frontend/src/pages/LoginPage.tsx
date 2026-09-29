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
            <div className="auth-page">
                <div className="auth-container">
                    <div className="auth-header">
                        <h1>Orion</h1>
                        <p>Sign in to continue</p>
                    </div>
                    
                

            

                    <form className="auth-form" onSubmit={handleSubmit}>
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
                            <p className="auth-error" role="alert">
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            className="auth-submit"
                            disabled={isSubmitting}
                        >
                            {isSubmitting
                                ? "Signing in..."
                                : "Sign in"
                            }
                        </button>
                    </form>
                
                    <p className="auth-footer">
                        Don't have an account?{" "}
                        <Link to="/register">
                            Create an account
                        </Link>
                    </p>
                </div>
            </div>
    );
}