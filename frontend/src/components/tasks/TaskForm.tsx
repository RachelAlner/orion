import { useState } from "react";
import type { SubmitEvent } from "react";

import {
    createTask, 
    updateTask,
} from "../../services/taskService";

import type { Task } from "../../types/task";

interface TaskFormProps {
    projectId: string;
    task?: Task;
    onSaved: () => void;
    onCancel: () => void;
}

export default function TaskForm({
    projectId, 
    task, 
    onSaved, 
    onCancel,
}: TaskFormProps) {
    const isEditing = task !== undefined;

    const [title, setTitle] = useState(
        task?.title ?? ""
    );

    const [description, setDescription] = useState(
        task?.description ?? ""
    );

    const [estimatedMinutes, setEstimatedMinutes] = 
        useState(
            task?.estimatedMinutes?.toString() ?? ""
        );

    const [deadline, setDeadline] = useState(
        task?.deadline 
            ? task.deadline.slice(0, 16)
            : ""
    );

    const [priority, setPriority] = useState(
        task?.priority?.toString() ?? ""
    );

    const [error, setError] = useState<string | null>(
        null
    );

    const [isSubmitting, setIsSubmitting] = 
        useState(false);

    async function handleSubmit(
        event: SubmitEvent     
    ) {
        event.preventDefault();

        setError(null);

        const trimmedTitle = title.trim();

        if (!trimmedTitle) {
            setError("Task title is required.");
            return;
        }

        if (trimmedTitle.length > 255) {
            setError(
                "Task title must be 255 characters or fewer."
            );
            return;
        }

        if (description.length > 2000) {
            setError(
                "Description must be 2,000 characters or fewer."
            );
            return;
        }

        const estimatedMinutesValue = 
            estimatedMinutes
                ? Number(estimatedMinutes)
                : null;
        
        if (
            estimatedMinutesValue !== null &&
            (!Number.isInteger(
                estimatedMinutesValue
            ) ||
                estimatedMinutesValue < 1)
        ) {
            setError(
                "Estimated time must be at least 1 minute."
            );

            return;
        }

        const priorityValue = priority 
            ? Number(priority)
            : null;
        
        if (
            priorityValue !== null && 
            (!Number.isInteger(priorityValue) ||
                priorityValue < 1 || 
                priorityValue > 5)
        ) {
            setError(
                "Priority must be between 1 and 5."
            );
            return;
        }

        setIsSubmitting(true);

        try {
            const request = {
                title: trimmedTitle, 
                description: description.trim() || null,
                estimatedMinutes: estimatedMinutesValue || null,
                deadline: deadline || null, 
                priority: priorityValue || null,
            };

            if (isEditing) {
                await updateTask(
                    projectId, 
                    task.id, 
                    request
                );
            } else {
                await createTask(
                    projectId, 
                    request
                );
            }

            onSaved();
        } catch (error) {
            console.error(error);

            setError(
                isEditing 
                    ? "Unable to update the task. Please try again."
                    : "Unable to create the task. Please try again."
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form 
            className="task-form"
            onSubmit={handleSubmit}>
            <div className="task-form-header">
                <h3>
                    {isEditing
                        ? "Edit task"
                        : "New task"}
                </h3>
            </div>

            <div>
                <label htmlFor="task-title">
                    Title
                </label>

                <input 
                    id="task-title"
                    type="text"
                    value={title}
                    onChange={(event) => 
                        setTitle(event.target.value)
                    }
                    maxLength={255}
                    required
                />
            </div>

            <div>
                <label htmlFor="task-description">
                    Description
                </label>

                <textarea 
                    id="task-description"
                    value={description}
                    onChange={(event) => 
                        setDescription(
                            event.target.value
                        )
                    }
                    maxLength={2000}
                />
            </div>

            <div>
                <label htmlFor="task-estimated-minutes">
                    Estimated time (minutes)
                </label>

                <input 
                    id="task-estimated-minutes"
                    type="number"
                    min="1"
                    value={estimatedMinutes}
                    onChange={(event) => 
                        setEstimatedMinutes(
                            event.target.value
                        )
                    }
                />
            </div>

            <div>
                <label htmlFor="task-deadline">
                    Deadline
                </label>

                <input 
                    id="task-deadline"
                    type="datetime-local"
                    value={deadline}
                    onChange={(event) =>
                        setDeadline(
                            event.target.value
                        )
                    }
                />
            </div>

            <div>
                <label htmlFor="task-priority">
                    Priority
                </label>

                <select 
                    id="task-priority"
                    value={priority}
                    onChange={(event) =>
                        setPriority(
                            event.target.value
                        )
                    }
                >
                    <option value="">
                        No priority
                    </option>

                    <option value="1">
                        1 - Lowest
                    </option>

                    <option value="2">
                        2
                    </option>

                    <option value="3">
                        3
                    </option>

                    <option value="4">
                        4
                    </option>

                    <option value="5">
                        5 - Highest 
                    </option>
                </select>
            </div>

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}

            <div>
                <button 
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting
                        ? isEditing 
                            ? "Saving..."
                            : "Creating"
                        : isEditing
                            ? "Save changes"
                            : "Create task"
                    }
                </button>

                <button
                    type="button"
                    onClick={onCancel}
                    disabled={isSubmitting}
                >
                    Cancel
                </button>
            </div>
        </form>
    );
}