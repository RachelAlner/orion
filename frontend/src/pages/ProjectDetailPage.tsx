import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import TaskForm from "../components/TaskForm";
import TaskList from "../components/TaskList";

import {
    deleteTask,
    getTasks,
} from "../services/taskService";

import type { Task } from "../types/task";

import { getProject } from "../services/projectService";
import type { Project } from "../types/project";

export default function ProjectDetailPage() {
    const { projectId } = useParams<{
        projectId: string;
    }>();

    const [project, setProject] = useState<Project | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [tasks, setTasks] = useState<Task[]>([]);
    const [isLoadingTasks, setIsLoadingTasks] = 
        useState(true);
    const [taskError, setTaskError] = 
        useState<string | null>(null);
    const [showCreateTaskForm, setShowCreateTaskForm] =
        useState(false);
    const [editingTask, setEditingTask] = 
        useState<Task | null>(null);
    const [deletingTaskId, setDeletingTaskId] = 
        useState<string | null>(null);

    async function loadTasks() {
        if (!projectId) {
            return;
        }

        try {
            setTaskError(error);

            const data = await getTasks(projectId);

            setTasks(data);
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to load the tasks. Please try again."
            );
        } finally {
            setIsLoadingTasks(false);
        }
    }
    
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
        loadTasks();
    }, [projectId]);

    async function handleDeleteTask(task: Task) {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${task.title}?`
        );

        if (!confirmed) {
            return;
        }

        if (!projectId) {
            return;
        }

        setDeletingTaskId(task.id);
        setTaskError(null);

        try {
            await deleteTask(
                projectId, 
                task.id
            );

            setTasks((currentTasks) =>
                currentTasks.filter(
                    (currentTask) =>
                        currentTask.id !== task.id
                )
            );
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to delete the task. Please try again."
            );
        } finally {
            setDeletingTaskId(null);
        }
    }

    function handleTaskCreated() {
        setShowCreateTaskForm(false);
        loadTasks();
    }

    function handleTaskUpdated() {
        setEditingTask(null);
        loadTasks();

    }

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
                
                {taskError && (
                    <p role="alert">
                        {taskError}
                    </p>
                )}

                {!showCreateTaskForm && 
                    editingTask === null && (
                        <button 
                            type="button"
                            onClick={() =>
                                setShowCreateTaskForm(true)
                            }
                        >
                            New task
                        </button>
                    )}
                
                {showCreateTaskForm && 
                    projectId && (
                    <TaskForm 
                        projectId={projectId}
                        onSaved={handleTaskCreated}
                        onCancel={() =>
                            setShowCreateTaskForm(false)
                        }
                    />
                
                )}

                {editingTask && projectId && (
                    <TaskForm 
                        projectId={projectId}
                        task={editingTask}
                        onSaved={handleTaskUpdated}
                        onCancel={() =>
                            setEditingTask(null)
                        }
                    />
                )}

                {isLoadingTasks ? (
                    <p>Loading tasks...</p>
                ) : (
                    <TaskList 
                        tasks={tasks}
                        onEdit={(task) => {
                            setShowCreateTaskForm(false);;
                            setEditingTask(task);
                        }}
                        onDelete={handleDeleteTask}
                        deletingTaskId={deletingTaskId}
                    />
                )}
            </section>
        </div>
    );
}