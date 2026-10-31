import type { Task } from "../../types/task";
import TaskForm from "./TaskForm";

import TaskDependencies from "./TaskDependencies";
import type { TaskDependency } from "../../types/taskDependency";

import PrerequisiteSelector from "./PrerequisiteSelector";

interface TaskDetailsProps {
    task: Task;
    tasks: Task[];
    dependencies: TaskDependency[];
    allDependencies: Record<string, TaskDependency[]>;
    isLoadingDependencies: boolean;
    showPrerequisiteSelector: boolean;
    dependencyError: string | null;
    onTaskClick: (task: Task) => void;
    onClose: () => void;
    onSaved: () => void;
    onComplete: (task: Task) => void;
    onDelete: (task: Task) => void;
    onRemoveDependency: (
        dependency: TaskDependency
    ) => void;
    onTogglePrerequisite: (task: Task) => void;
    onTogglePrerequisiteSelector: () => void;
}

export default function TaskDetails({
    task,
    tasks,
    dependencies,
    allDependencies,
    isLoadingDependencies,
    showPrerequisiteSelector,
    dependencyError,
    onTaskClick,
    onClose,
    onSaved,
    onComplete,
    onDelete,
    onRemoveDependency,
    onTogglePrerequisite,
    onTogglePrerequisiteSelector,
}: TaskDetailsProps) {
    const isCompleted = task.status === "COMPLETED";

    const existingPrerequisiteIds = new Set(
        dependencies.map(
            (dependency) => dependency.dependsOnTaskId
        )
    );

    return (
        <div
            className="task-details-overlay"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget) {
                    onClose();
                }
            }}
        >
            {showPrerequisiteSelector && (
                <div className="prerequisite-selector-positioner">
                    <PrerequisiteSelector
                        tasks={tasks}
                        currentTask={task}
                        existingPrerequisiteIds={existingPrerequisiteIds}
                        allDependencies={allDependencies}
                        onToggle={onTogglePrerequisite}
                        onClose={onTogglePrerequisiteSelector}
                    />
                </div>
            )}

            <section className="task-details">
                <button
                    type="button"
                    className="task-details-close"
                    onClick={onClose}
                    aria-label="Close tasks details"
                >
                    ×
                </button>

                <div className="task-details-title">
                    <button
                        type="button"
                        className={`task-complete-circle ${
                            isCompleted
                                ? "completed"
                                : ""
                        }`}
                        onClick={() => onComplete(task)}
                        aria-label={
                            isCompleted
                                ? "Task completed"
                                : "Mark task as complete"
                        }
                    >
                        {isCompleted && "✓"}
                    </button>
                </div>

                <TaskForm
                    projectId={task.projectId}
                    task={task}
                    onSaved={onSaved}
                    onCancel={onClose}
                />

                {dependencyError && (
                    <p className="task-error" role="alert">
                        {dependencyError}
                    </p>
                )}

                <TaskDependencies
                    task={task}
                    tasks={tasks}
                    dependencies={dependencies}
                    allDependencies={allDependencies}
                    isLoading={isLoadingDependencies}
                    showPrerequisiteSelector={showPrerequisiteSelector}
                    onTogglePrerequisiteSelector={onTogglePrerequisiteSelector}
                    onRemove={onRemoveDependency}
                    onTaskClick={onTaskClick}
                />

                <div className="task-details-actions">
                    <button
                        type="button"
                        className="task-cancel-button"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        onClick={() => onDelete(task)}
                        className="task-delete-button"
                    >
                        Delete
                    </button>
                </div>
            </section>
        </div>
    );
}