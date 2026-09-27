import { NavLink, Outlet, useNavigate } from "react-router-dom";

import { useAuth } from "../hooks/AuthContext";

function AppLayout() {
    const { logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="app-layout">
            <header className="app-header">
                <h1>Orion</h1>

                <button 
                    type="button"
                    onClick={handleLogout}
                >
                    Log out
                </button>
            </header>

            <div className="app-body">
                <aside className="app-sidebar">
                    <nav>
                        <NavLink 
                            to="/dashboard"
                            className="app-nav-link"
                        >
                            Dashboard
                        </NavLink>

                        <NavLink 
                            to="/projects"
                            className="app-nav-link"
                        >
                            Projects
                        </NavLink>

                        <NavLink 
                            to="/schedule"
                            className="app-nav-link"
                        >
                            Schedule
                        </NavLink>
                    </nav>
                </aside>

                <main className="app-main">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default AppLayout;