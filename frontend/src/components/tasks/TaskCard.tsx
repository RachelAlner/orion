import type { Task } from "../../types/task";

interface TaskCardProps {
    task: Task;
    onClick: (task: Task) => void;
}

export default function TaskCard({
    task, 
    onClick,
}: TaskCardProps) {
    return (
        <button
            type="button"
            className="task-card"
            onClick={() => onClick(task)}
        >
            <div className="task-card-title">
                {task.title}
            </div>

            <div className="task-card-status">
                {task.status.replace("_", " ")}
            </div>

            <div className="task-card-time">
                {task.estimatedMinutes !== null 
                    ? `${task.workedMinutes} / ${task.estimatedMinutes} min`
                    : `${task.workedMinutes} min`
                }
            </div>

            <div className="task-card-remaining">
                {task.remainingMinutes !== null
                    ? `${task.remainingMinutes} min left`
                    : "No estimate"
                }

            </div>
        </button>
    );
}