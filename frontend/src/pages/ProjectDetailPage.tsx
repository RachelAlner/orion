import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import { getProject } from "../services/projectService";
import type { Project } from "../types/project";

export default function ProjectDetailPage() {
    const { projectId } = useParams<{
        projectId: string;
    }>();

    const [project, setProject] = useState<Project | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadProject() {
            if (!projectId) {
                setError("Project not found.");
                setIsLoading(false);
                return;
            }

            try {
                setError(null);

                const data = 
                    await getProject(projectId);

                setProject(data);
            } catch (error) {
                console.error(error);

                setError(
                    "Unable to load the project. Please try again."
                );
            } finally {
                setIsLoading(false);
            }
        }

        loadProject();
    }, [projectId]);

    if (isLoading) {
        return (
            <div> 
                <p>Loading project...</p>
            </div>
        )
    }

    if (error || !project) {
        return (
            <div>
                <h1>Project not found</h1>

                <p role="alert">
                    {error ?? 
                        "The project could not be found."
                    }
                </p>

                <Link to="/projects">
                    Back to projects
                </Link>
            </div>
        );
    }

    return (
        <div>
            <Link to="/projects">
                ← Back to projects
            </Link>

            <header>
                <h1>{project.name}</h1>

                {project.description && (
                    <p>{project.description}</p>
                )}
            </header>

            <section>
                <h2>Project details</h2>

                <dl>
                    <div>
                        <dt>Status</dt>
                        <dd>{project.status}</dd>
                    </div>

                    <div>
                        <dt>Priority</dt>
                        <dd>
                            {project.priority ??
                                "No priority"
                            }
                        </dd>
                    </div>

                    <div>
                        <dt>Deadline</dt>
                        <dd>
                            {project.deadline ??
                                "No deadline"
                            }
                        </dd>
                    </div>

                    <div>
                        <dt>Created</dt>
                        <dd>
                            {new Date(
                                project.createdAt
                            ).toLocaleString()}
                        </dd>
                    </div>

                    <div>
                        <dt>Last updated</dt>
                        <dd>
                            {new Date(
                                project.updatedAt
                            ).toLocaleString()}
                        </dd>
                    </div>
                </dl>
            </section>

            <section>
                <h2>Tasks</h2>

                <p>Tasks...</p>
            </section>
        </div>
    );
}