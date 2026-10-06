import type { Task } from "../../types/task";
import TaskCard from "./TaskCard";

import type { DragEvent } from "react";

type DropPosition = "above" | "below" | null;

interface TaskListProps {
    tasks: Task[];
    onTaskClick: (task: Task) => void;
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
    dropTarget: {
        taskId: string;
        position: DropPosition;
    } | null;
    taskDependencyDepth: (
        taskId: string 
    ) => number;
}

export default function TaskList({
    tasks, 
    onTaskClick,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDrop,
    dropTarget,
    taskDependencyDepth,
}: TaskListProps) {
    if (tasks.length === 0) {
        return (
            <p>
                No tasks yet.
            </p>
        );
    }

    return (
        <div className="task-list">
            {tasks.map((task) => (
                <TaskCard 
                    key={task.id}
                    task={task}
                    onClick={onTaskClick}
                    onDragStart={onDragStart}
                    onDragEnd={onDragEnd}
                    onDragOver={onDragOver}
                    onDrop={onDrop}
                    dropPosition={
                        dropTarget?.taskId === task.id 
                            ? dropTarget.position
                            : null
                    }
                    dependencyDepth={
                        taskDependencyDepth(task.id)
                    }
                />
            ))}
        </div>
    );
}