import { useState } from "react";
import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";
import styles from "./PrerequisiteSelector.module.css";

interface PrerequisiteSelectorProps {
    tasks: Task[];
    currentTask: Task;
    existingPrerequisiteIds: Set<string>;
    allDependencies: Record<string, TaskDependency[]>;
    onToggle: (task: Task) => void;
    onClose: () => void;
}

export default function PrerequisiteSelector({
    tasks,
    currentTask,
    existingPrerequisiteIds,
    allDependencies,
    onToggle,
    onClose,
}: PrerequisiteSelectorProps) {
    const [searchQuery, setSearchQuery] = useState("");

    const availableTasks = tasks.filter(
        (task) => task.id !== currentTask.id
    );

    const query = searchQuery.trim().toLocaleLowerCase();

    const filteredTasks = availableTasks.filter((task) =>
        task.title.toLowerCase().includes(query)
    );

    const selectedTasks = filteredTasks.filter((task) =>
        existingPrerequisiteIds.has(task.id)
    );

    const unselectedTasks = filteredTasks.filter(
        (task) => !existingPrerequisiteIds.has(task.id)
    );

    function wouldCreateCycle(candidateTaskId: string): boolean {
        const visited = new Set<string>();
        const stack = [candidateTaskId];

        while (stack.length > 0) {
            const taskId = stack.pop()!;

            if (taskId === currentTask.id) {
                return true;
            }

            if (visited.has(taskId)) {
                continue;
            }

            visited.add(taskId);

            for (const dependency of allDependencies[taskId] ?? []) {
                stack.push(dependency.dependsOnTaskId);
            }
        }
        
        return false;
    }

    if (availableTasks.length === 0) {
        return (
            <div className={styles.prerequisiteSelectorPanel}>
                <p className={styles.prerequisiteSelectorEmpty}>
                    No other tasks available.
                </p>
            </div>
        );
    }

    return (
        <div className={styles.prerequisiteSelectorPanel}>
            <div className={styles.prerequisiteSelectorHeader}>
                <h3>Prerequisites</h3>
                <button
                    type="button"
                    className={styles.prerequisiteSelectorClose}
                    onClick={onClose}
                    aria-label="Close prerequisite selector"
                >
                    ×
                </button>
            </div>
            <div className={styles.prerequisiteSelectorSearch}>
                <input
                    type="text"
                    placeholder="Search tasks..."
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    className={styles.prerequisiteSelectorInput}
                    aria-label="Search tasks"
                />
            </div>

            {filteredTasks.length === 0 ? (
                <p className={styles.prerequisiteSelectorEmpty}>
                    No tasks match your search.
                </p>
            ) : (
                <>
                    {selectedTasks.length > 0 && (
                        <div className={styles.prerequisiteSelectorGroup}>
                            <h4 className={styles.prerequisiteSelectorGroupTitle}>
                                Selected ({selectedTasks.length})
                            </h4>
                            {selectedTasks.map((task) => (
                                <label
                                    key={task.id}
                                    className={`${styles.prerequisiteSelectorItem} ${styles.prerequisiteSelectorItemSelected}`}
                                >
                                    <input
                                        type="checkbox"
                                        checked
                                        onChange={() => onToggle(task)}
                                    />
                                    <div className={styles.prerequisiteSelectorItemContent}>
                                        <span className={styles.prerequisiteSelectorItemTitle}>
                                            {task.title}
                                        </span>
                                        <span className={styles.prerequisiteSelectorItemStatus}>
                                            {task.status.replace("_", " ")}
                                        </span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    )}

                    {unselectedTasks.length > 0 && (
                        <div className={styles.prerequisiteSelectorGroup}>
                            <h4 className={styles.prerequisiteSelectorGroupTitle}>
                                Available ({unselectedTasks.length})
                            </h4>
                            {unselectedTasks.map((task) => {
                                const isBlocked = wouldCreateCycle(task.id);

                                return (
                                    <label
                                        key={task.id}
                                        className={`${styles.prerequisiteSelectorItem} ${
                                            isBlocked
                                                ? styles.prerequisiteSelectorItemBlocked
                                                : ""
                                        } ${
                                            task.status === "COMPLETED"
                                                ? styles.prerequisiteSelectorItemCompleted
                                                : ""
                                        }`}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={false}
                                            disabled={isBlocked}
                                            onChange={() => onToggle(task)}
                                        />
                                        <div className={styles.prerequisiteSelectorItemContent}>
                                            <span className={styles.prerequisiteSelectorItemTitle}>
                                                {task.title}
                                            </span>
                                            <span className={styles.prerequisiteSelectorItemStatus}>
                                                {task.status.replace("_", " ")}
                                            </span>
                                        </div>
                                        {isBlocked && (
                                            <span className={styles.prerequisiteSelectorItemWarning}>
                                                Would create cycle
                                            </span>
                                        )}
                                    </label>
                                );
                            })}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}