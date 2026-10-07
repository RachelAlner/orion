import type { Task } from "../../types/task";
import TaskForm from "./TaskForm";

import TaskDependencies from "./TaskDependencies";
import type { TaskDependency } from "../../types/taskDependency";

interface TaskDetailsProps {
    task: Task;
    tasks: Task[];
    dependencies: TaskDependency[];
    isLoadingDependencies: boolean;
    onClose: () => void;
    onSaved: () => void;
    onComplete: (task: Task) => void;
    onDelete: (task: Task) => void;
    onRemoveDependency: (
        dependency: TaskDependency
    ) => void;
}

export default function TaskDetails({
    task, 
    tasks,
    dependencies,
    isLoadingDependencies,
    onClose, 
    onSaved, 
    onComplete,
    onDelete,
    onRemoveDependency,
}: TaskDetailsProps) {
    const isCompleted = task.status === "COMPLETED";

    return (
        <div
            className="task-details-overlay"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget) {
                    onClose();
                }
            }}
        >
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

                <TaskDependencies
                    tasks={tasks}
                    dependencies={dependencies}
                    isLoading={isLoadingDependencies}
                    onRemove={onRemoveDependency}
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