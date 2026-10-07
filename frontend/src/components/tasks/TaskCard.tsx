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
                    {task.title}
                </div>

                <div className="task-card-status">
                    {task.status.replace("_", " ")}
                </div>

                <div className="task-card-time">
                    {task.estimatedMinutes !== null
                        ? `${task.workedMinutes} / ${task.estimatedMinutes} min`
                        : `${task.workedMinutes} min worked`
                    }
                </div>

                <div className="task-card-remaining">
                    {task.remainingMinutes !== null
                        ? task.remainingMinutes === 0
                            ? "Completed"
                            : `${task.remainingMinutes} min left`
                        : null
                    }
                </div>
            </button>
        </div>
    );
}