import type { Task } from "../types/task";

import ProgressForm from "../components/ProgressForm";

interface TaskListProps {
    tasks: Task[];
    onEdit: (task: Task) => void;
    onDelete: (task: Task) => void;
    onProgress: (task: Task) => void;
    onProgressSaved: (task: Task) => void;
    onCancelProgress: () => void;
    onComplete: (task: Task) => void;
    progressTask: Task | null;
    deletingTaskId: string | null;
}

export default function TaskList({
    tasks, 
    onEdit, 
    onDelete,
    onProgress,
    onProgressSaved,
    onCancelProgress,
    onComplete,
    progressTask,
    deletingTaskId,
}: TaskListProps) {
    if (tasks.length === 0) {
        return (
            <p>
                This project doesn't have any tasks yet.
            </p>
        );
    }

    return (
        <div>
            {tasks.map((task) => (
                <article key={task.id}>
                    <h3>{task.title}</h3>

                    {task.description && (
                        <p>{task.description}</p>
                    )}

                    <p>
                        Status: {task.status}
                    </p>

                    <p>
                        Estimated:{" "}
                        {task.estimatedMinutes !== null
                            ? `${task.estimatedMinutes} minutes`
                            : "No estimate"}
                    </p>

                    <p>
                        Worked: {task.workedMinutes} minutes
                    </p>

                    <p>
                        Remaining:{" "}
                        {task.remainingMinutes !== null
                            ? `${task.remainingMinutes} minutes`
                            : "No estimate"}
                    </p>

                    <p>
                        Deadline:{" "}
                        {task.deadline ??
                            "No deadline"}
                    </p>

                    <p>
                        Priority:{" "}
                        {task.priority ?? 
                            "No priority"}
                    </p>

                    {task.status !== "COMPLETED" && 
                        task.status !== "CANCELLED" && (
                            <button 
                                type="button"
                                onClick={() => 
                                    onComplete(task)
                                }
                            >
                                Complete
                            </button>
                        )}

                    {task.estimatedMinutes !== null && 
                        task.status !== "COMPLETED" && 
                        task.status !== "CANCELLED" && (
                            <button 
                                type="button"
                                onClick={() => onProgress(task)}
                            >
                                Update progress
                            </button>
                        )}

                    <button
                        type="button"
                        onClick={() => onEdit(task)}>
                        Edit 
                    </button>

                    <button
                        type="button"
                        onClick={() =>
                            onDelete(task)
                        }
                        disabled={
                            deletingTaskId === task.id
                        }
                        >
                            {deletingTaskId === task.id
                                ? "Deleting..."
                                : "Delete"}
                    </button>

                    {progressTask?.id === task.id && (
                    <ProgressForm 
                        projectId={task.projectId}
                        task={task}
                        onSaved={onProgressSaved}
                        onCancel={() => {
                            {onCancelProgress}
                        }}
                    />
                )}
                </article>
            ))}
        </div>
    );
}