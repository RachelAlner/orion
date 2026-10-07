import type { Task } from "../../types/task";
import type { 
    DragEvent, 
    CSSProperties,
} from "react";

type DropPosition = "above" | "below" | null;

interface TaskCardProps {
    task: Task;
    onClick: (task: Task) => void;
    onComplete: (task: Task) => void;
    onDragStart: (task: Task) => void;
    onDragEnd: () => void;
    onDragOver: (
        event: DragEvent<HTMLDivElement>,
        task: Task
    ) => void;
    onDrop: (
        event: DragEvent<HTMLDivElement>,
        task: Task
    ) => void;
    dropPosition: DropPosition;
    dependencyDepth: number;
}

const MINUTES_PER_HOUR = 60;
const MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
const MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY;
const MINUTES_PER_MONTH = 30 * MINUTES_PER_DAY;
const MINUTES_PER_YEAR = 365 * MINUTES_PER_DAY;

function formatDuration(minutes: number): string {
    if (minutes === 0) {
        return "0m";
    }

    let remaining = minutes;
    const parts: string[] = [];

    const years = Math.floor(
        remaining / MINUTES_PER_YEAR
    );
    remaining %= MINUTES_PER_YEAR;

    const months = Math.floor(
        remaining / MINUTES_PER_MONTH
    );
    remaining %= MINUTES_PER_MONTH;

    const weeks = Math.floor(
        remaining / MINUTES_PER_WEEK
    );
    remaining %= MINUTES_PER_WEEK;

    const days = Math.floor(
        remaining / MINUTES_PER_DAY
    );
    remaining %= MINUTES_PER_DAY;

    const hours = Math.floor(
        remaining / MINUTES_PER_HOUR
    );
    remaining %= MINUTES_PER_HOUR;

    const remainingMinutes = remaining;

    if (years > 0) {
        parts.push(`${years}y`);
    }

    if (months > 0) {
        parts.push(`${months}mo`);
    }

    if (weeks > 0) {
        parts.push(`${weeks}w`);
    }

    if (days > 0) {
        parts.push(`${days}d`);
    }

    if (hours > 0) {
        parts.push(`${hours}h`);
    }

    if (remainingMinutes > 0) {
        parts.push(`${remainingMinutes}m`);
    }
    
    return parts.join(" ");
}

export default function TaskCard({
    task, 
    onClick,
    onComplete,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDrop,
    dropPosition,
    dependencyDepth,
}: TaskCardProps) {
    const isCompleted = task.status === "COMPLETED";

    return (
        <div
            className={`task-card 
                ${isCompleted
                    ? "task-card-completed"
                    : ""
                }
                ${dropPosition
                    ? `task-card-drop-${dropPosition}`
                    : ""
                }
            `}
            style={{
                "--task-depth": dependencyDepth,
            } as CSSProperties} 
            draggable={!isCompleted}
            onDragStart={() => onDragStart(task)}
            onDragEnd={onDragEnd}
            onDragOver={(event) => onDragOver(event, task)}
            onDrop={(event) => onDrop(event, task)}
        >
            <button
                type="button"
                className={`task-complete-circle ${
                    isCompleted ? "completed" : ""
                }`}
                onClick={(event) => {
                    event.stopPropagation();
                    onComplete(task);
                }}
                aria-label={
                    isCompleted
                        ? "Task completed"
                        : "Mark task as complete"
                }
            >
                {isCompleted && "✓"}
            </button>

            <button
                type="button"
                className="task-card-main"
                onClick={() => onClick(task)}
            >

                <div className="task-card-title">
                    <span>{task.title}</span>
                    {task.priority !== null && (
                        <span
                            className={`task-priority task-priority-${task.priority}`}
                        >
                            P{task.priority}
                        </span>
                    )}
                </div>

                <div className="task-card-status">
                    {task.status.replace("_", " ")}
                </div>

                <div className="task-card-time">
                    {task.estimatedMinutes !== null
                        ? `${formatDuration(task.workedMinutes)} / ${formatDuration(task.estimatedMinutes)}`
                        : `${formatDuration(task.workedMinutes)} worked`
                    }
                </div>

                <div className="task-card-remaining">
                    {task.remainingMinutes !== null
                        ? task.remainingMinutes === 0
                            ? "Completed"
                            : `${formatDuration(task.remainingMinutes)} left`
                        : null
                    }
                </div>

                <div className="task-card-deadline">
                    {task.deadline
                        ? new Date(task.deadline).toLocaleDateString(
                            undefined,
                            {
                                day: "numeric",
                                month: "short",
                                year: "numeric",
                            }
                        )
                        : "No deadline"
                    }

                </div>
            </button>
        </div>
    );
}