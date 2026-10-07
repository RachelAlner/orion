import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";


import { useAuth } from "../../hooks/AuthContext";
import { getProjects } from "../../services/projectService";
import type { Project } from "../../types/project";

function AppLayout() {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const [projects, setProjects] = useState<Project[]>([]);
    const [projectsError, setProjectsError] = useState<string | null>(null);

    useEffect(() => {
        async function loadProjects() {
            try {
                const loadedProjects = await getProjects();
                setProjects(loadedProjects);
            } catch (error) {
                console.error(error);
                setProjectsError("Unable to load projects.");
            }
        }

        loadProjects();
    }, []);


    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="app-layout">
            <header className="app-header">
                <h1>Orion.</h1>

                <button 
                    type="button"
                    className="logout-button"
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

                        <div className="sidebar-projects">
                            <div className="sidebar-section-header">
                                <span>Projects</span>

                                <NavLink
                                    to="/projects"
                                    className="sidebar-add-project"
                                    aria-label="View projects"
                                >
                                    +
                                </NavLink>
                            </div>

                            <div className="sidebar-project-list">
                                {projectsError && (
                                    <p className="sidebar-error">
                                        {projectsError}
                                    </p>
                                )}

                                {projects.map((project) => (
                                    <NavLink
                                        key={project.id}
                                        to={`/projects/${project.id}`}
                                        className="sidebar-project-link"
                                    >
                                        {project.name}
                                    </NavLink>
                                ))}
                            </div>
                        </div>

                        <NavLink 
                            to="/schedule"
                            className="app-nav-link"
                        >
                            Schedule
                        </NavLink>

                        <NavLink 
                            to="/availability"
                            className="app-nav-link"
                        >
                            Availability
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