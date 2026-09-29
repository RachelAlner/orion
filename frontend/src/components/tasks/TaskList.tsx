import type { Task } from "../../types/task";
import TaskCard from "./TaskCard";

interface TaskListProps {
    tasks: Task[];
    onTaskClick: (task: Task) => void;
}

export default function TaskList({
    tasks, 
    onTaskClick,
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
                />
            ))}
        </div>
    );
}