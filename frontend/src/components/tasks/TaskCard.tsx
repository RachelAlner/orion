import type { Task } from "../../types/task";
import type { 
    DragEvent, 
    CSSProperties,
} from "react";

type DropPosition = "above" | "below" | null;

interface TaskCardProps {
    task: Task;
    onClick: (task: Task) => void;
    onDragStart: (task: Task) => void;
    onDragEnd: () => void;
    onDragOver: (
        event: DragEvent<HTMLButtonElement>,
        task: Task
    ) => void;
    onDrop: (
        event: DragEvent<HTMLButtonElement>,
        task: Task
    ) => void;
    dropPosition: DropPosition;
    dependencyDepth: number;
}

export default function TaskCard({
    task, 
    onClick,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDrop,
    dropPosition,
    dependencyDepth,
}: TaskCardProps) {
    return (
        <button
            type="button"
            className={`task-card ${dropPosition
                ? "task-card-drop-${dropPosition}"
                : ""
            }`}
            style={{
                "--task-depth": dependencyDepth,
            } as CSSProperties} 
            draggable
            onClick={() => onClick(task)}
            onDragStart={() => onDragStart(task)}
            onDragEnd={onDragEnd}
            onDragOver={(event) => onDragOver(event, task)}
            onDrop={(event) => onDrop(event, task)}
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