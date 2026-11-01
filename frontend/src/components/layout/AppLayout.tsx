import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";


import { useAuth } from "../../hooks/AuthContext";
import { getProjects } from "../../services/projectService";
import type { Project } from "../../types/project";
import styles from "./AppLayout.module.css";

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
        <div className={styles.appLayout}>
            <header className={styles.appHeader}>
                <h1>Orion.</h1>

                <button
                    type="button"
                    className={styles.logoutButton}
                    onClick={handleLogout}
                >
                    Log out
                </button>
            </header>

            <div className={styles.appBody}>
                <aside className={styles.appSidebar}>
                    <nav>
                        <NavLink
                            to="/dashboard"
                            className={({ isActive }) => `${styles.appNavLink} ${isActive ? styles.active : ''}`}
                        >
                            Dashboard
                        </NavLink>

                        <div className={styles.sidebarProjects}>
                            <div className={styles.sidebarSectionHeader}>
                                <span>Projects</span>

                                <NavLink
                                    to="/projects"
                                    className={styles.sidebarAddProject}
                                    aria-label="View projects"
                                >
                                    +
                                </NavLink>
                            </div>

                            <div className={styles.sidebarProjectList}>
                                {projectsError && (
                                    <p className={styles.sidebarError}>
                                        {projectsError}
                                    </p>
                                )}

                                {projects.map((project) => (
                                    <NavLink
                                        key={project.id}
                                        to={`/projects/${project.id}`}
                                        className={({ isActive }) => `${styles.sidebarProjectLink} ${isActive ? styles.active : ''}`}
                                    >
                                        {project.name}
                                    </NavLink>
                                ))}
                            </div>
                        </div>

                        <NavLink
                            to="/schedule"
                            className={({ isActive }) => `${styles.appNavLink} ${isActive ? styles.active : ''}`}
                        >
                            Schedule
                        </NavLink>

                        <NavLink
                            to="/availability"
                            className={({ isActive }) => `${styles.appNavLink} ${isActive ? styles.active : ''}`}
                        >
                            Availability
                        </NavLink>
                    </nav>
                </aside>

                <main className={styles.appMain}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default AppLayout;