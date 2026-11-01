import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";
import TaskCard from "./TaskCard";

import type { DragEvent } from "react";
import styles from "./TaskList.module.css";

interface TaskListProps {
    tasks: Task[];
    taskDependencies: Record<string, TaskDependency[]>;
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
    dropTargetTaskId: string | null;
}

export default function TaskList({
    tasks, 
    taskDependencies,
    onTaskClick,
    onComplete,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDrop,
    dropTargetTaskId,
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

    function getDependencyCounts(taskId: string) {
        const prerequisiteCount = 
            (taskDependencies[taskId] ?? []).length;

        const dependentTaskIds = new Set(
            Object.entries(taskDependencies)
                .filter(([, dependencies]) =>
                    dependencies.some(
                        (dependency) => 
                           dependency.dependsOnTaskId === taskId
                    )
                )
                .map(([dependentTaskId]) => dependentTaskId)

        );

        return {
            prerequisiteCount, 
            dependentCount: dependentTaskIds.size,
        };
    }

    return (
        <div className={styles.taskList}>
            {activeTasks.map((task) => {
                const {
                    prerequisiteCount,
                    dependentCount,
                } = getDependencyCounts(task.id);

                return (
                    <TaskCard
                        key={task.id}
                        task={task}
                        onClick={onTaskClick}
                        onComplete={onComplete}
                        onDragStart={onDragStart}
                        onDragEnd={onDragEnd}
                        onDragOver={onDragOver}
                        onDrop={onDrop}
                        prerequisiteCount={prerequisiteCount}
                        dependentCount={dependentCount}
                        isDropTarget={
                            dropTargetTaskId === task.id
                        }
                    />
                    );
                })}

                {completedTasks.length > 0 && (
                    <section className={styles.completedTaskSection}>
                        <h3 className={styles.completedTaskSectionTitle}>
                            Completed
                        </h3>

                        <div className={styles.completedTaskList}>
                            {completedTasks.map((task) => {
                                const {
                                    prerequisiteCount,
                                    dependentCount,
                                } = getDependencyCounts(task.id);

                                return (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        onClick={onTaskClick}
                                        onComplete={onComplete}
                                        onDragStart={onDragStart}
                                        onDragEnd={onDragEnd}
                                        onDragOver={onDragOver}
                                        onDrop={onDrop}
                                        prerequisiteCount={prerequisiteCount}
                                        dependentCount={dependentCount}
                                        isDropTarget={false}
                                    />
                                );
                            })}
                        </div>
                    </section>
                )}
        </div>
    );
}