import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import ProjectForm from "../components/ProjectForm";
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
        setEditingProject(null);
        loadProjects();
    }

    if (isLoading) {
        return (
            <div>
                <h1>Projects</h1>
                <p>Loading projects...</p>
            </div>
        );
    }

    return (
        <div>
            <h1>Projects</h1>

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}

            {!showCreateForm && 
                editingProject === null && (
                    <button 
                        type="button"
                        onClick={() => 
                            setShowCreateForm(true)
                        }
                    >
                        New project
                    </button>
                )}
            
            {showCreateForm && (
                <ProjectForm 
                    onSaved={handleCreateSaved}
                    onCancel={() => 
                        setShowCreateForm(false)
                    }
                />
            )}

            {editingProject && (
                <ProjectForm 
                    project={editingProject}
                    onSaved={handleEditSaved}
                    onCancel={() => 
                        setEditingProject(null)
                    }
                />
            )}

            {projects.length === 0 ? (
                <p>
                    You don't have any projects yet.
                </p>
            ) : (
                <div>
                    {projects.map((project) => (
                        <article key={project.id}>
                            <h2>
                                <Link to={`/projects/${project.id}`}>
                                    {project.name}
                                </Link>
                            </h2>

                            {project.description && (
                                <p>{project.description}</p>
                            )}

                            <p>
                                Deadline: {" "}
                                {project.deadline ?? 
                                    "No deadline"}
                            </p>

                            <p>
                                Priority:{" "}
                                {project.priority ?? 
                                    "No priority"}
                            </p>

                            <p>
                                Status: {project.status}
                            </p>

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
                        </article>
                    ))}
                </div>
            )
            
            }
        </div>
    );
}