import type { ReactNode } from "react";
import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";
import styles from "./TaskDependencies.module.css";

interface TaskDependenciesProps {
    task: Task;
    tasks: Task[];
    dependencies: TaskDependency[];
    allDependencies: Record<string, TaskDependency[]>;
    onRemove: (dependency: TaskDependency) => void;
    onTaskClick: (task: Task) => void;
    isLoading: boolean;
    showPrerequisiteSelector: boolean;
    onTogglePrerequisiteSelector: () => void;
    children?: ReactNode;
}

export default function TaskDependencies({
    task,
    tasks,
    dependencies,
    allDependencies,
    onRemove,
    onTaskClick,
    isLoading,
    showPrerequisiteSelector,
    onTogglePrerequisiteSelector,
    children,
}: TaskDependenciesProps) {
    const prerequisiteTasks = dependencies
        .map((dependency) =>
            tasks.find(
                (candidate) =>
                    candidate.id === dependency.dependsOnTaskId
            )
        )
        .filter(
            (item): item is Task =>
                item !== undefined
        );

    const dependentTaskIds = new Set(
        Object.entries(allDependencies)
            .filter(([, taskDependencies]) =>
                taskDependencies.some(
                    (dependency) =>
                        dependency.dependsOnTaskId === task.id
                )
            )
            .map(([taskId]) => taskId)
    );

    const dependentTasks = Array.from(dependentTaskIds)
        .map((taskId) =>
            tasks.find((candidate) => candidate.id === taskId)
        )
        .filter(
            (item): item is Task =>
                item !== undefined
        );

    const prerequisiteCount = prerequisiteTasks.length;
    const dependentCount = dependentTasks.length;

    return (
        <section className={styles.taskDependencies}>
            <div className={styles.taskDependenciesHeader}>
                <h3>Dependencies</h3>
                <span className={styles.taskDependenciesCount}>
                    {prerequisiteCount + dependentCount}
                </span>
            </div>

            {isLoading ? (
                <p className={styles.taskDependenciesEmpty}>
                    Loading dependencies...
                </p>
            ) : (
                <>
                    <div className={styles.taskDependenciesGroup}>
                        <div className={styles.taskDependenciesGroupHeader}>
                            <h4>Requires</h4>
                            {prerequisiteCount > 0 && (
                                <span className={styles.taskDependenciesGroupCount}>
                                    {prerequisiteCount}
                                </span>
                            )}
                        </div>

                        {prerequisiteCount === 0 ? (
                            <p className={styles.taskDependenciesEmpty}>
                                No prerequisites.
                            </p>
                        ) : (
                            <div className={styles.taskDependenciesList}>
                                {prerequisiteTasks.map(
                                    (prerequisiteTask) => {
                                        const dependency =
                                            dependencies.find(
                                                (item) =>
                                                    item.dependsOnTaskId ===
                                                    prerequisiteTask.id
                                            );

                                        if (!dependency) {
                                            return null;
                                        }

                                        return (
                                            <div
                                                key={prerequisiteTask.id}
                                                className={styles.taskDependencyItem}
                                            >
                                                <button
                                                    type="button"
                                                    className={styles.taskDependencyTask}
                                                    onClick={() =>
                                                        onTaskClick(
                                                            prerequisiteTask
                                                        )
                                                    }
                                                >
                                                    <span className={styles.taskDependencyTaskTitle}>
                                                        {prerequisiteTask.title}
                                                    </span>
                                                    <span className={styles.taskDependencyTaskStatus}>
                                                        {prerequisiteTask.status.replace(
                                                            "_", " "
                                                        )}
                                                    </span>
                                                </button>

                                                <button
                                                    type="button"
                                                    className={styles.taskDependencyRemove}
                                                    onClick={() =>
                                                        onRemove(dependency)
                                                    }
                                                    aria-label={`Remove prerequisite ${prerequisiteTask.title}`}
                                                >
                                                    ×
                                                </button>
                                            </div>
                                        );
                                    }
                                )}
                            </div>
                        )}
                    </div>

                    <div className={styles.taskPrerequisiteControl}>
                        <button
                            type="button"
                            className={styles.taskAddPrerequisite}
                            onClick={onTogglePrerequisiteSelector}
                            aria-expanded={showPrerequisiteSelector}
                        >
                            + Add prerequisite
                            <span aria-hidden="true">
                                {showPrerequisiteSelector ? "⌃" : "⌄"}
                            </span>
                        </button>

                        {showPrerequisiteSelector && children}
                    </div>


                    {dependentCount > 0 && (
                        <div className={styles.taskDependenciesGroup}>
                            <div className={styles.taskDependenciesGroupHeader}>
                                <h4>Blocks</h4>
                                <span className={styles.taskDependenciesGroupCount}>
                                    {dependentCount}
                                </span>
                            </div>

                            <div className={styles.taskDependenciesList}>
                                {dependentTasks.map((dependentTask) => (
                                    <div
                                        key={dependentTask.id}
                                        className={`${styles.taskDependencyItem} ${styles.taskDependencyItemBlocks}`}
                                    >
                                        <button
                                            type="button"
                                            className={styles.taskDependencyTask}
                                            onClick={() =>
                                                onTaskClick(dependentTask)
                                            }
                                        >
                                            <span className={styles.taskDependencyTaskTitle}>
                                                {dependentTask.title}
                                            </span>
                                            <span className={styles.taskDependencyTaskStatus}>
                                                {dependentTask.status.replace(
                                                    "_"," "
                                                )}
                                            </span>
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </>
            )}
        </section>
    );
}