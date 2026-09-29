import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { register } from "../services/authService";
import { useAuth } from "../hooks/AuthContext";

export default function RegisterPage() {
    const navigate = useNavigate();
    const { loginWithToken } = useAuth();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(
        event: React.SubmitEvent
    ) {
        event.preventDefault();

        setError(null);

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setIsSubmitting(true);

        try {
            const response = await register(
                email, 
                password
            );

            loginWithToken(response.token);

            navigate("/dashboard", {
                replace: true,
            });
        } catch (error) {
            console.error(error);

            setError(
                "Unable to create your account. Please check your details and try again."
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
                        <p>Create your account</p>
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

                        <div>
                            <label htmlFor="confirm-password">
                                Confirm password
                            </label>

                            <input 
                                id="confirm-password"
                                type="password"
                                value={confirmPassword}
                                onChange={(event) => 
                                    setConfirmPassword(
                                        event.target.value
                                    )
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
                        Already have an account?{" "}
                        <Link to="/login">
                            Sign in
                        </Link>
                    </p>
                </div>
            </div>
    );
}