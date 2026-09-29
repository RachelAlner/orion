import type { Task } from "../../types/task";
import TaskForm from "./TaskForm";
import ProgressForm from "./ProgressForm";

interface TaskDetailsProps {
    task: Task;
    onClose: () => void;
    onSaved: () => void;
    onProgressSaved: (task: Task) => void;
    onComplete: (task: Task) => void;
    onDelete: (task: Task) => void;
}

export default function TaskDetails({
    task, 
    onClose, 
    onSaved, 
    onProgressSaved,
    onComplete,
    onDelete,
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