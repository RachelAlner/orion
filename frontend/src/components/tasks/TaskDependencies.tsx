import type { Task } from "../../types/task";
import type { TaskDependency } from "../../types/taskDependency";

interface TaskDependenciesProps {
    tasks: Task[];
    dependencies: TaskDependency[];
    onRemove: (dependency: TaskDependency) => void;
    isLoading: boolean;
}

export default function TaskDependencies({
    tasks, 
    dependencies,
    onRemove,
    isLoading,
}: TaskDependenciesProps) {
    const dependencyTasks = dependencies
        .map((dependency) => 
        tasks.find(
            (candidate) => 
                    candidate.id === dependency.dependsOnTaskId
        )
    )
    .filter(
        (dependencyTask): dependencyTask is Task => 
            dependencyTask !== undefined
    );

    return (
        <section className="task-dependencies">
            <div className="task-dependencies-header">
                <h3>Dependencies</h3>
                <span>
                    {dependencies.length}
                </span>
            </div>

            {isLoading ? (
                <p className="task-dependencies-empty">
                    Loading dependencies...
                </p>
            ) : dependencyTasks.length === 0 ? (
                <p className="task-dependencies-empty">
                    No dependencies.
                </p>
            ) : (
                <div className="task-dependencies-list">
                    {dependencyTasks.map((dependencyTask) => {
                        const dependency = 
                            dependencies.find(
                                (item) => 
                                    item.dependsOnTaskId ===
                                    dependencyTask.id
                            );
                        
                        if (!dependency) {
                            return null;
                        }

                        return (
                            <div
                                key={dependencyTask.id}
                                className="task-dependency-item"
                            >
                                <div>
                                    <span className="task-dependency-label">
                                        Depends on
                                    </span>

                                    <strong>
                                        {dependencyTask.title}
                                    </strong>
                                </div>

                                <button
                                    type="button"
                                    className="task-dependency-remove"
                                    onClick={() => 
                                        onRemove(dependency)
                                    }
                                    aria-label={`Remove dependency on ${dependencyTask.title}`}
                                >
                                    ×
                                </button>
                            </div>
                        );
                    })}
                </div>
            )
            }
        </section>
    );
}