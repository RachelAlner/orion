import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import {
    deleteTask, 
    getTasks, 
    completeTask,
} from "../services/taskService";

import TaskForm from "../components/tasks/TaskForm";
import TaskList from "../components/tasks/TaskList";
import TaskDetails from "../components/tasks/TaskDetails";

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
    const [selectedTask, setSelectedTask] = 
        useState<Task | null>(null);

    async function loadTasks() {
        if (!projectId) {
            return;
        }

        try {
            setTaskError(null);

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

    async function handleTaskSaved() {
        if (!projectId) {
            return;
        }

        try {
            const updatedTasks = await getTasks(projectId);

            setTasks(updatedTasks);

            const updatedTask = updatedTasks.find(
                (task) => task.id === selectedTask?.id
            );

            if (updatedTask) {
                setSelectedTask(updatedTask);
            }
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to reload the task. Please try again."
            );
        }
    }

    async function handleDeleteTask(task: Task) {
        const confirmed = window.confirm(
            `Are you sure you want to delete "${task.title}"?`
        );

        if (!confirmed) {
            return;
        }

        if (!projectId) {
            return;
        }

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

            setSelectedTask(null);
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to delete the task. Please try again."
            );
        } 
    }

    async function handleCompleteTask(task: Task) {
        try {
            const updatedTask = await completeTask(
                task.projectId,
                task.id
            );

            setTasks((currentTasks) => 
                currentTasks.map((currentTask) => 
                    currentTask.id === updatedTask.id 
                        ? updatedTask 
                        : currentTask
                )
            );

            setSelectedTask(updatedTask);
        } catch (error) {
            console.error(error);
        }
    }

    function handleTaskClick(task: Task) {
        setSelectedTask(task);
    }

    function handleCloseTaskDetails() {
        setSelectedTask(null);
    }

    function handleTaskCreated() {
        setShowCreateTaskForm(false);
        loadTasks();
    }
    
    function handleProgressSaved(updatedTask: Task) {
        setTasks((currentTasks) => 
            currentTasks.map((task) => 
                task.id === updatedTask.id
                    ? updatedTask 
                    : task
            )
        );

        setSelectedTask(updatedTask);
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
        <div className="project-page">
            <Link 
                to="/projects"
                className="project-back-link"
            >
                ← Projects
            </Link>

            <section className="project-overview">

                <header className="project-header">
                    <h1>{project.name}</h1>

                    {project.description && (
                        <p>{project.description}</p>
                    )}

                </header>

                <dl className="project-details">
                    <div>
                        <dt>Deadline</dt>
                        <dd>
                            {project.deadline 
                                ? new Date(
                                    project.deadline
                                ).toLocaleDateString()
                                : "No deadline"
                            }
                        </dd>
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
                        <dt>Status</dt>
                        <dd>{project.status.replace("_", " ")}</dd>
                    </div>
                </dl>
            </section>

            <section className="project-tasks">
                <div className="project-tasks-header">
                    <h2>Tasks</h2>

                    {!showCreateTaskForm && (
                        <button
                            type="button"
                            onClick={() => setShowCreateTaskForm(true)}
                        >
                            + New task
                        </button>
                    )}
                </div>

                {showCreateTaskForm && projectId && (
                    <div className="new-task-form">
                        <TaskForm 
                            projectId={projectId}
                            onSaved={handleTaskCreated}
                            onCancel={() => setShowCreateTaskForm(false)}
                        />
                    </div>
                )}

                {taskError && (
                    <p className="task-error" role="alert">
                        {taskError}
                    </p>
                )}

                {isLoadingTasks ? (
                    <p>Loading tasks...</p>
                ) : (
                    <TaskList 
                        tasks={tasks}
                        onTaskClick={handleTaskClick}
                    />
                )}

                {selectedTask && (
                    <TaskDetails
                        task={selectedTask}
                        onClose={handleCloseTaskDetails}
                        onSaved={handleTaskSaved}
                        onProgressSaved={handleProgressSaved}
                        onComplete={handleCompleteTask}
                        onDelete={handleDeleteTask}
                    />
                )}
            </section>
        </div>
    );
}