import type { ReactNode } from "react";
import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";

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
        <section className="task-dependencies">
            <div className="task-dependencies-header">
                <h3>Dependencies</h3>
                <span className="task-dependencies-count">
                    {prerequisiteCount + dependentCount}
                </span>
            </div>

            {isLoading ? (
                <p className="task-dependencies-empty">
                    Loading dependencies...
                </p>
            ) : (
                <>
                    <div className="task-dependencies-group">
                        <div className="task-dependencies-group-header">
                            <h4>Requires</h4>
                            {prerequisiteCount > 0 && (
                                <span className="task-dependencies-group-count">
                                    {prerequisiteCount}
                                </span>
                            )}
                        </div>

                        {prerequisiteCount === 0 ? (
                            <p className="task-dependencies-empty">
                                No prerequisites.
                            </p>
                        ) : (
                            <div className="task-dependencies-list">
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
                                                className="task-dependency-item"
                                            >
                                                <button
                                                    type="button"
                                                    className="task-dependency-task"
                                                    onClick={() =>
                                                        onTaskClick(
                                                            prerequisiteTask
                                                        )
                                                    }
                                                >
                                                    <span className="task-dependency-task-title">
                                                        {prerequisiteTask.title}
                                                    </span>
                                                    <span className="task-dependency-task-status">
                                                        {prerequisiteTask.status.replace(
                                                            "_", " "
                                                        )}
                                                    </span>
                                                </button>

                                                <button
                                                    type="button"
                                                    className="task-dependency-remove"
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

                    <div className="task-prerequisite-control">
                        <button
                            type="button"
                            className="task-add-prerequisite"
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
                        <div className="task-dependencies-group">
                            <div className="task-dependencies-group-header">
                                <h4>Blocks</h4>
                                <span className="task-dependencies-group-count">
                                    {dependentCount}
                                </span>
                            </div>

                            <div className="task-dependencies-list">
                                {dependentTasks.map((dependentTask) => (
                                    <div
                                        key={dependentTask.id}
                                        className="task-dependency-item task-dependency-item-blocks"
                                    >
                                        <button
                                            type="button"
                                            className="task-dependency-task"
                                            onClick={() =>
                                                onTaskClick(dependentTask)
                                            }
                                        >
                                            <span className="task-dependency-task-title">
                                                {dependentTask.title}
                                            </span>
                                            <span className="task-dependency-task-status">
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