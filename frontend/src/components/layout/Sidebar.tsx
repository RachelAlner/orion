import { Link, useLocation } from "react-router-dom";
import styles from "./Sidebar.module.css";


export default function Sidebar() {
    const location = useLocation();

    const isProjectsActive =
        location.pathname.startsWith("/projects");
        
    return (
        <aside className={styles.sidebar}>
            <div className={styles.sidebarLogo}>
                <Link to="/projects">
                    Orion
                </Link>
            </div>

            <nav className={styles.sidebarNavigation}>
                <Link
                    to="/projects"
                    className={`${styles.sidebarProjectLink} ${isProjectsActive ? styles.active : ''}`}
                >
                    Projects
                </Link>

                <Link
                    to="/schedule"
                    className={styles.sidebarLink}
                >
                    Schedule
                </Link>

                <Link 
                    to="/availability"
                    className={styles.sidebarLink}
                >
                    Availability
                </Link>
            </nav>
        </aside>
    );
}