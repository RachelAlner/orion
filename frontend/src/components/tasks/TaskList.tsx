import type { Task } from "../../types/task";
import TaskCard from "./TaskCard";

import type { DragEvent } from "react";

type DropPosition = "above" | "below" | null;

interface TaskListProps {
    tasks: Task[];
    onTaskClick: (task: Task) => void;
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
    onComplete,
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

    const activeTasks = tasks.filter(
        (task) => task.status !== "COMPLETED"
    );

    const completedTasks = tasks.filter(
        (task) => task.status === "COMPLETED"
    );

    return (
        <div className="task-list">
            {activeTasks.map((task) => (
                <TaskCard 
                    key={task.id}
                    task={task}
                    onClick={onTaskClick}
                    onComplete={onComplete}
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

            {completedTasks.length > 0 && (
                <section className="completed-task-section">
                    <h3 className="completed task-section-title">
                        Completed
                    </h3>

                    <div className="completed-task-list">
                        {completedTasks.map((task) => (
                            <TaskCard 
                                key={task.id}
                                task={task}
                                onClick={onTaskClick}
                                onComplete={onComplete}
                                onDragStart={onDragStart}
                                onDragEnd={onDragEnd}
                                onDragOver={onDragOver}
                                onDrop={onDrop}
                                dropPosition={null}
                                dependencyDepth={0}
                            />
                        ))}
                    </div>
                </section>
            )}
        </div>
    );
}