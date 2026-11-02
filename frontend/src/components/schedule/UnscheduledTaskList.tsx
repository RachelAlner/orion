import type { Task } from "../../types/task";
import type {
    UnscheduledReason,
    UnscheduledTask,
} from "../../types/schedule";
import styles from "./UnscheduledTaskList.module.css";


interface UnscheduledTaskListProps {
    tasks: Task[];
    unscheduledTasks: UnscheduledTask[];
}

function getReasonLabel(
    reason: UnscheduledReason
): string {
    switch (reason) {
        case "DEPENDENCY_BLOCKED":
            return "Dependency blocked";
        case "DEADLINE_UNACHIEVABLE":
            return "Deadline cannot be met";
        case "INSUFFICIENT_AVAILABILITY":
            return "Not enough available time";
    }
}

function formatRemainingMinutes(
    minutes: number 
): string {
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (hours === 0) {
        return `${remainingMinutes} min remaining`;
    }

    if (remainingMinutes === 0) {
        return `${hours}h remaining`;
    }

    return `${hours}h ${remainingMinutes}m remaining`;
}

export default function UnscheduledTaskList({
    tasks, 
    unscheduledTasks,
}: UnscheduledTaskListProps) {

    if (unscheduledTasks.length === 0) {
        return null;
    }

    const tasksById = new Map(
        tasks.map((task) => [task.id, task])
    );

    return (
        <section className={`${styles.scheduleSection} ${styles.scheduleWarningSection}`}>
            <div className={styles.scheduleSectionHeader}>
                <h2>Needs attention</h2>
                <span>{unscheduledTasks.length}</span>
            </div>

            <div className={styles.unscheduledTaskList}>
                {unscheduledTasks.map((unscheduledTask) => {
                    const task = tasksById.get(unscheduledTask.taskId);

                    return (
                        <div 
                            key={unscheduledTask.taskId}
                            className={styles.unscheduledTask}
                        > 
                            <div>
                                <strong>
                                    {task?.title ??
                                        "Unknown task"}
                                </strong>

                                <span>
                                    {formatRemainingMinutes(
                                        unscheduledTask.remainingMinutes
                                    )}
                                </span>
                            </div>

                            <span className={styles.unscheduledTaskReason}>
                                {getReasonLabel(
                                    unscheduledTask.reason
                                )}
                            </span>
                        </div>
                    );
                })}
            </div>
        </section>
    );
}