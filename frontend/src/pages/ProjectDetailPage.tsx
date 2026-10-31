import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import {
    deleteTask, 
    getTasks, 
    completeTask,
} from "../services/taskService";

import { 
    addTaskDependency,
    getTaskDependencies,
    deleteTaskDependency,
} from "../services/taskDependencyService";

import type { TaskDependency } from "../types/taskDependency";

import TaskForm from "../components/tasks/TaskForm";
import TaskList from "../components/tasks/TaskList";
import TaskDetails from "../components/tasks/TaskDetails";

import type { Task } from "../types/task";
import type { DragEvent } from "react";

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

    const [dependencyError, setDependencyError] =
        useState<string | null>(null);

    const [draggedTask, setDraggedTask] = 
        useState<Task | null>(null);

    const [dropTargetTaskId, setDropTargetTaskId] = 
        useState<string | null>(null);
        
    const [dependencies, setDependencies] = 
        useState<TaskDependency[]>([]);

    const [isLoadingDependencies, setIsLoadingDependencies] =
        useState(false);

    const [taskDependencies, setTaskDependencies] =
        useState<Record<string, TaskDependency[]>>({});

    const [showPrerequisiteSelector, setShowPrerequisiteSelector] =
        useState(false);

    async function loadTasks() {
        if (!projectId) {
            return;
        }

        try {
            setTaskError(null);

            const data = await getTasks(projectId);

            setTasks(data);

            await loadAllTaskDependencies(data);
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to load the tasks. Please try again."
            );
        } finally {
            setIsLoadingTasks(false);
        }
    }

    async function loadTaskDependencies(taskId: string) {
        if (!projectId) {
            return;
        }

        try {
            setIsLoadingDependencies(true);
            setDependencyError(null);

            const data = await getTaskDependencies(
                projectId,
                taskId
            );

            setDependencies(data);
        } catch (error) {
            console.error(error);

            setDependencyError(
                "Unable to load task dependencies."
            );
        } finally {
            setIsLoadingDependencies(false);
        }
    }
    
    async function loadAllTaskDependencies(
        projectTasks: Task[]
    ) {
        if (!projectId) {
            return;
        }

        try {
            const entries = await Promise.all(
                projectTasks.map(async (task) => {
                    const dependencies = 
                        await getTaskDependencies(
                            projectId, 
                            task.id
                        );
                    return [task.id, dependencies] as const;
                })
            );

            setTaskDependencies(
                Object.fromEntries(entries)
            );
        } catch (error) {
            console.error(error);

            setDependencyError(
                "Unable to load task dependencies."
            );
        }
    }

    async function handleTogglePrerequisite(
        prerequisiteTask: Task
    ) {
        if (!projectId || !selectedTask) {
            return;
        }

        const existingDependency = dependencies.find(
            (dependency) => 
                dependency.dependsOnTaskId === 
                prerequisiteTask.id
        );

        try {
            setDependencyError(null);

            if (existingDependency) {
                await deleteTaskDependency(
                    projectId, 
                    selectedTask.id,
                    prerequisiteTask.id
                );
            } else {
                await addTaskDependency(
                    projectId, 
                    selectedTask.id, 
                    prerequisiteTask.id
                );
            }

            await loadAllTaskDependencies(tasks);
            await loadTaskDependencies(selectedTask.id);
        } catch (error) {
            console.error(error);

            if (
                error instanceof Error &&
                error.message.includes("DEPENDENCY_CYCLE")
            ) {
                setDependencyError(
                    "This dependency would create a cycle."
                );
            } else if (
                error instanceof Error &&
                error.message.includes(
                    "TASK_DEPENDENCY_ALREADY_EXISTS"
                )
            ) {
                setDependencyError(
                    "This prerequisite already exists."
                );
            } else {
                setDependencyError(
                    "Unable to update this prerequisite."
                );
            }
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

            await loadAllTaskDependencies(updatedTasks);

            if (selectedTask) {
                const updatedTask = updatedTasks.find(
                    (task) => task.id === selectedTask.id
                );

                if (updatedTask) {
                    setSelectedTask(updatedTask);
                } else {
                    handleCloseTaskDetails();
                }
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

        if (!confirmed || !projectId) {
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

            setTaskDependencies((currentDependencies) => {
                const updatedDependencies = {
                    ...currentDependencies,
                }; 

                delete updatedDependencies[task.id];

                for (const [dependentTaskId, dependencies] of Object.entries(
                    updatedDependencies
                )) {
                    updatedDependencies[dependentTaskId] = 
                        dependencies.filter(
                            (dependency) => 
                                dependency.dependsOnTaskId !== task.id
                        );
                }

                return updatedDependencies;
            })

            setSelectedTask(null);
            setDependencies([]);
            setShowPrerequisiteSelector(false);
        } catch (error) {
            console.error(error);

            setTaskError(
                "Unable to delete the task. Please try again."
            );
        } 
    }

    async function handleCompleteTask(task: Task) {
        if (!projectId) {
            return;
        }

        try {
            setTaskError(null);

            const updatedTask = await completeTask(
                projectId,
                task.id
            );
            
            setTasks((currentTasks) => 
                currentTasks.map((currentTask) => 
                    currentTask.id === updatedTask.id 
                        ? updatedTask 
                        : currentTask
                )
            );

            setSelectedTask((currentTask) =>
                currentTask?.id === updatedTask.id 
                    ? updatedTask
                    : currentTask
            );
        } catch (error) {
            console.error(error);
            setTaskError(
                task.status === "COMPLETED"
                    ? "Unable to mark the task as incomplete. Please try again."
                    : "Unable to complete the task. Please try again."
            );
        }
    }

    async function handleRemoveDependency(
        dependency: TaskDependency
    ) {
        if (!projectId || !selectedTask) {
            return;
        }

        try {
            setDependencyError(null);

            await deleteTaskDependency(
                projectId, 
                selectedTask.id,
                dependency.dependsOnTaskId
            );

            setDependencies((currentDependencies) => 
                currentDependencies.filter(
                    (currentDependency) => 
                        !(
                            currentDependency.taskId ===
                                dependency.taskId &&
                            currentDependency.dependsOnTaskId === 
                                dependency.dependsOnTaskId
                        )
                )
            );

            await loadAllTaskDependencies(tasks);
        } catch (error) {
            console.error(error);

            setDependencyError(
                "Unable to remove this dependency."
            );
        }
    }

    function handleTaskClick(task: Task) {
        setSelectedTask(task);
        setShowPrerequisiteSelector(false);
        loadTaskDependencies(task.id);
    }

    function handleCloseTaskDetails() {
        setSelectedTask(null);
        setDependencies([]);
        setDependencyError(null);
        setShowPrerequisiteSelector(false);
    }


    function handleTaskCreated() {
        setShowCreateTaskForm(false);
        loadTasks();
    }     

    function handleTaskDragStart(task: Task) {
        setDraggedTask(task);
        setDropTargetTaskId(null);
        setDependencyError(null);
    }

    function handleTaskDragEnd() {
        setDraggedTask(null);
        setDropTargetTaskId(null);
    }

    function handleTaskDragOver(
        event: DragEvent<HTMLDivElement>,
        targetTask: Task
    ) {
        event.preventDefault();

        if (!draggedTask) {
            return;
        }

        if (draggedTask.id === targetTask.id) {
            setDropTargetTaskId(null);
            return;
        }

        setDropTargetTaskId(targetTask.id);
    }

    async function handleTaskDrop(
        event: DragEvent<HTMLDivElement>,
        targetTask: Task
    ) {
        event.preventDefault();

        if (!draggedTask || !projectId) {
            return;
        }

        if (draggedTask.id === targetTask.id) {
            return;
        }

        try {
            setDependencyError(null);

            await addTaskDependency(
                projectId, 
                draggedTask.id, 
                targetTask.id
            );

            await loadAllTaskDependencies(tasks);

            if (selectedTask?.id === draggedTask.id) {
                await loadTaskDependencies(draggedTask.id);
            }
        } catch (error) {
            console.error(error);
            
            if (
                error instanceof Error &&
                error.message.includes("TASK_DEPENDENCY_ALREADY_EXISTS")
            ) {
                setDependencyError(
                    "This dependency already exists."
                );
            } else if (
                error instanceof Error &&
                    error.message.includes("DEPENDENCY_CYCLE")
                ) {
                setDependencyError(
                    "This dependency would create a cycle."
                );
            } else {
                setDependencyError(
                    "Unable to create this dependency."
                );
            }
        } finally {
            setDraggedTask(null);
            setDropTargetTaskId(null);
        }
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
                    <div 
                        className="form-overlay"
                        onMouseDown={(event) => {
                            if (event.target === event.currentTarget) {
                                setShowCreateTaskForm(false);
                            }
                        }}
                    >
                        <div className="form-overlay-panel">
                            <button 
                                type="button"
                                className="form-overlay-close"
                                onClick={() => 
                                    setShowCreateTaskForm(false)
                                }
                                aria-label="Close"
                            >
                                ×
                            </button>

                            <TaskForm 
                                projectId={projectId}
                                onSaved={handleTaskCreated}
                                onCancel={() => setShowCreateTaskForm(false)}
                            />
                        </div>
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
                    <>
                        {dependencyError && (
                            <p className="task-error" role="alert">
                                {dependencyError}
                            </p>
                        )}
                    
                        <TaskList 
                            tasks={tasks}
                            taskDependencies={taskDependencies}
                            onTaskClick={handleTaskClick}
                            onComplete={handleCompleteTask}
                            onDragStart={handleTaskDragStart}
                            onDragEnd={handleTaskDragEnd}
                            onDragOver={handleTaskDragOver}
                            onDrop={handleTaskDrop}
                            dropTargetTaskId={dropTargetTaskId}
                        />
                    </>
                )}

                {selectedTask && (
                    <TaskDetails
                        task={selectedTask}
                        tasks={tasks}
                        dependencies={dependencies}
                        allDependencies={taskDependencies}
                        isLoadingDependencies={isLoadingDependencies}
                        showPrerequisiteSelector={showPrerequisiteSelector}
                        dependencyError={dependencyError}
                        onTaskClick={handleTaskClick}
                        onClose={handleCloseTaskDetails}
                        onSaved={handleTaskSaved}
                        onComplete={handleCompleteTask}
                        onDelete={handleDeleteTask}
                        onRemoveDependency={handleRemoveDependency}
                        onTogglePrerequisite={handleTogglePrerequisite}
                        onTogglePrerequisiteSelector={() =>
                            setShowPrerequisiteSelector(
                                (current) => !current
                            )
                        }
                    />
                )}
            </section>
        </div>
    );
}