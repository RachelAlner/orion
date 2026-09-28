import type { Task } from "../types/task";

interface TaskListProps {
    tasks: Task[];
    onEdit: (task: Task) => void;
    onDelete: (task: Task) => void;
    deletingTaskId: string | null;
}

export default function TaskList({
    tasks, 
    onEdit, 
    onDelete,
    deletingTaskId
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
                        {task.estimatedMinutes ??
                            "No estimate"}{" "}
                        {task.estimatedMinutes
                            ? "minutes"
                            : ""}
                    </p>

                    <p>
                        Remaining:{" "}
                        {task.remainingMinutes ??
                            "No estimate"}{" "}
                        {task.remainingMinutes !== null
                            ? "minutes"
                            : ""}
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
                </article>
            ))}
        </div>
    );
}