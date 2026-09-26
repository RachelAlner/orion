import { NavLink, Outlet } from "react-router-dom";

function AppLayout() {
    return (
        <div className="app-layout">
            <header className="app-header">
                <h1>Orion</h1>
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
            </div>

            <main className="app-main">
                <Outlet />
            </main>
        </div>
    );
}

export default AppLayout;