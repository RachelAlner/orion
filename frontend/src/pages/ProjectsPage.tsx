import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import ProjectForm from "../components/projects/ProjectForm";
import { deleteProject, getProjects } from "../services/projectService";
import type { Project } from "../types/project";

export default function ProjectsPage() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editingProject, setEditingProject] = useState<Project | null>(null);
    const [deletingProjectId, setDeletingProjectId] = useState<string | null>(null);

    async function loadProjects() {
        try {
            setError(null);

            const data = await getProjects();

            setProjects(data);

        } catch (error) {
            console.error(error);

            setError(
                "Unable to load your projects. Please try again"
            );
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        loadProjects();
    }, []);

    async function handleDelete(project: Project) {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${project.name}"?`
        );

        if (!confirmed) {
            return;
        }

        setDeletingProjectId(project.id);
        setError(null);

        try {
            await deleteProject(project.id);

            setProjects((currentProjects) => 
                currentProjects.filter(
                    (currentProject) => 
                        currentProject.id !==
                    project.id
                )
            );
        } catch (error) {
            console.error(error);

            setError(
                "Unable to delete the project. Please try again."
            );
        } finally {
            setDeletingProjectId(null);
        }
    }

    function handleCreateSaved() {
        setShowCreateForm(false);
        loadProjects();
    }

    function handleEditSaved() {
        loadProjects();
    }

    function handleCloseCreate() {
        setShowCreateForm(false);
    }

    function handleCloseEdit() {
        setEditingProject(null);
    }

    if (isLoading) {
        return (
            <div className="projects-page">
                <div>
                    <h1>Projects</h1>
                    <p>Loading projects...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="projects-page">
            <header className="projects-header">
                <div>
                    <h1>Projects</h1>
                    <p>
                        Organise your work and track your progress.
                    </p>
                </div>
            
                <button 
                    type="button"
                    className="projects-new-button"
                    onClick={() => {
                        setEditingProject(null);
                        setShowCreateForm(true);
                    }}
                >
                    + New Project 
                </button>
            </header>        

            {error && (
                <p className="projects-error" role="alert">
                    {error}
                </p>
            )}

            {showCreateForm && (
                <div 
                    className="form-overlay"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            handleCloseCreate();
                        }
                    }}
                >
                    <div className="form-overlay-panel">
                        <button 
                            type="button"
                            className="form-overlay-close"
                            onClick={handleCloseCreate}
                            aria-label="Close"
                        >
                            ×
                        </button>
                        <ProjectForm 
                            onSaved={handleCreateSaved}
                            onCancel={handleCloseCreate}
                        />
                    </div>
                </div>
            )}

            {editingProject && (
                <div 
                    className="project-details-overlay"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            handleCloseEdit();
                        }
                    }}
                >
                    <aside className="project-details-edit">
                        <button 
                            type="button"
                            className="project-details-close"
                            onClick={handleCloseEdit}
                            aria-label="Close"
                        >
                            ×
                        </button>

                        <ProjectForm
                            project={editingProject}
                            onSaved={handleEditSaved}
                            onCancel={handleCloseEdit}
                        />
                    </aside>
                </div>
            )}

            {projects.length === 0 ? (
                    <div className="projects-empty">
                        <p>No projects yet.</p>

                        <button 
                            type="button"
                            onClick={() =>
                                setShowCreateForm(true)
                            }
                        >
                            Create your first project
                        </button>
                    </div>
                ) : (
                    <div className="project-list">
                        {projects.map((project) => (
                            <div
                                key={project.id}
                                className="project-list-item"
                            >
                                <div className="project-list-main">
                                    <Link
                                        to={`/projects/${project.id}`}
                                        className="project-list-link"
                                    >
                                        <h2>{project.name}</h2>

                                        {project.description && (
                                            <p>{project.description}</p>
                                        )}
                                    </Link>

                                    <div className="project-list-details">
                                        <span>
                                            <strong>
                                                Deadline
                                            </strong>

                                            {project.deadline
                                                ? new Date(
                                                    project.deadline
                                                ).toLocaleDateString()
                                                : "No deadline"
                                            }
                                         </span>

                                        <span>
                                            <strong>
                                                Status
                                            </strong>

                                               {project.status.replace("_", " ")}
                                        </span>
                                    </div>
                                </div>

                                <div className="project-list-actions">
                                    <button 
                                        type="button"
                                        onClick={() => {
                                            setShowCreateForm(false);
                                            setEditingProject(project);
                                        }}
                                    >
                                        Edit 
                                     </button>

                                    <button 
                                        type="button"
                                        onClick={() => 
                                            handleDelete(project)
                                        }
                                        disabled={
                                            deletingProjectId === 
                                            project.id
                                        }
                                    >
                                        {deletingProjectId ===
                                        project.id 
                                            ? "Deleting..."
                                            : "Delete"
                                        }
                                    </button>

                                    <Link
                                        to={`/projects/${project.id}`}
                                        className="project-list-arrow"
                                        aria-label={`Open ${project.name}`}
                                    >
                                        →
                                    </Link>
                                </div>
                            </div>

                        ))}
                    </div>
                )}
        </div>
    );
}