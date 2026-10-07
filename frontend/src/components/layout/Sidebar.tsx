import { Link, useLocation } from "react-router-dom";

export default function Sidebar() {
    const location = useLocation();

    const isProjectsActive =
        location.pathname.startsWith("/projects");
        
    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <Link to="/projects">
                    Orion
                </Link>
            </div>

            <nav className="sidebar-navigation">
                <Link
                    to="/projects"
                    className={
                        isProjectsActive 
                            ? "sidebar-link active"
                            : "sidebar-link"
                    }
                >
                    Projects
                </Link>

                <Link
                    to="/schedule"
                    className="sidebar-link"
                >
                    Schedule
                </Link>

                <Link 
                    to="/availability"
                    className="sidebar-link"
                >
                    Availability
                </Link>
            </nav>
        </aside>
    );
}