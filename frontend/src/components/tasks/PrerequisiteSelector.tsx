import { useState } from "react";
import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";

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
            <div className="prerequisite-selector-panel">
                <p className="prerequisite-selector-empty">
                    No other tasks available.
                </p>
            </div>
        );
    }

    return (
        <div className="prerequisite-selector-panel">
            <div className="prerequisite-selector-header">
                <h3>Prerequisites</h3>
                <button
                    type="button"
                    className="prerequisite-selector-close"
                    onClick={onClose}
                    aria-label="Close prerequisite selector"
                >
                    ×
                </button>
            </div>
            <div className="prerequisite-selector-search">
                <input
                    type="text"
                    placeholder="Search tasks..."
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    className="prerequisite-selector-input"
                    aria-label="Search tasks"
                />
            </div>

            {filteredTasks.length === 0 ? (
                <p className="prerequisite-selector-empty">
                    No tasks match your search.
                </p>
            ) : (
                <>
                    {selectedTasks.length > 0 && (
                        <div className="prerequisite-selector-group">
                            <h4 className="prerequisite-selector-group-title">
                                Selected ({selectedTasks.length})
                            </h4>
                            {selectedTasks.map((task) => (
                                <label
                                    key={task.id}
                                    className="prerequisite-selector-item prerequisite-selector-item-selected"
                                >
                                    <input
                                        type="checkbox"
                                        checked
                                        onChange={() => onToggle(task)}
                                    />
                                    <div className="prerequisite-selector-item-content">
                                        <span className="prerequisite-selector-item-title">
                                            {task.title}
                                        </span>
                                        <span className="prerequisite-selector-item-status">
                                            {task.status.replace("_", " ")}
                                        </span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    )}

                    {unselectedTasks.length > 0 && (
                        <div className="prerequisite-selector-group">
                            <h4 className="prerequisite-selector-group-title">
                                Available ({unselectedTasks.length})
                            </h4>
                            {unselectedTasks.map((task) => {
                                const isBlocked = wouldCreateCycle(task.id);

                                return (
                                    <label
                                        key={task.id}
                                        className={`prerequisite-selector-item ${
                                            isBlocked
                                                ? "prerequisite-selector-item-blocked"
                                                : ""
                                        } ${
                                            task.status === "COMPLETED"
                                                ? "prerequisite-selector-item-completed"
                                                : ""
                                        }`}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={false}
                                            disabled={isBlocked}
                                            onChange={() => onToggle(task)}
                                        />
                                        <div className="prerequisite-selector-item-content">
                                            <span className="prerequisite-selector-item-title">
                                                {task.title}
                                            </span>
                                            <span className="prerequisite-selector-item-status">
                                                {task.status.replace("_", " ")}
                                            </span>
                                        </div>
                                        {isBlocked && (
                                            <span className="prerequisite-selector-item-warning">
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