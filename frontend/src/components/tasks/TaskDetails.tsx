import type { Task } from "../../types/task";
import TaskForm from "./TaskForm";
import ProgressForm from "./ProgressForm";

import TaskDependencies from "./TaskDependencies";
import type { TaskDependency } from "../../types/taskDependency";

interface TaskDetailsProps {
    task: Task;
    tasks: Task[];
    dependencies: TaskDependency[];
    isLoadingDependencies: boolean;
    onClose: () => void;
    onSaved: () => void;
    onProgressSaved: (task: Task) => void;
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
    onProgressSaved,
    onComplete,
    onDelete,
    onRemoveDependency,
}: TaskDetailsProps) {
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

                <h2>{task.title}</h2>

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

                {task.estimatedMinutes !== null && 
                    task.status !== "COMPLETED" && 
                    task.status !== "CANCELLED" && (
                        <ProgressForm 
                            projectId={task.projectId}
                            task={task}
                            onSaved={onProgressSaved}
                            onCancel={onClose}
                        />
                    )
                }

                <div className="task-details-actions">
                    {task.status !== "COMPLETED" &&
                        task.status !== "CANCELLED" && (
                            <button
                                type="button"
                                onClick={() => onComplete(task)}
                                className="task-complete-button"
                            >
                                Complete
                            </button>
                    )}

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