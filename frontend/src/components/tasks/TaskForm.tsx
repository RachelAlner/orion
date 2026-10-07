import { useState, useEffect } from "react";
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

    const [workedMinutes, setWorkedMinutes] = useState(
        task?.workedMinutes?.toString() ?? ""
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

    const [isSaving, setIsSaving] = useState(false);
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

    async function handleCreate(
        event: SubmitEvent     
    ) {
        event.preventDefault();

        setError(null);

        const validationError = validateFields();

        if (validationError) {
            setError(validationError);
            return;
        }

        setIsSaving(true);

        try {
            await createTask(projectId, {
                title: title.trim(),
                description: description.trim() || null,
                estimatedMinutes: estimatedMinutes
                    ? Number(estimatedMinutes)
                    : null,
                deadline: deadline ?? null,
                priority: priority 
                    ? Number(priority)
                    : null,
            });

            onSaved();
        } catch (error) {
            console.error(error);

            setError(
                "Unable to create the task. Please try again."
            );
        } finally {
            setIsSaving(false);
        }
    }

    function validateFields(): string | null {
        const trimmedTitle = title.trim();

        if (!trimmedTitle) {
            return "Task title is required.";
        }

        if (trimmedTitle.length > 255) {
            return(
                "Task title must be 255 characters or fewer."
            );
        }

        if (description.length > 2000) {
            return(
                "Description must be 2,000 characters or fewer."
            );
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
            return(
                "Estimated time must be at least 1 minute."
            );
        }

        const workedMinutesValue =
            workedMinutes
                ? Number(workedMinutes) 
                : null;

        if (
            workedMinutesValue !== null &&
            (!Number.isInteger(workedMinutesValue) ||
            workedMinutesValue < 0)
        ) {
            return(
                "Minutes worked must be a whole number."
            );
        }

        if (
            estimatedMinutesValue !== null &&
            workedMinutesValue !== null &&
            workedMinutesValue > estimatedMinutesValue
        ) {
            return(
                "Minutes worked cannot exceed estimated minutes."
            );
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
            return(
                "Priority must be between 1 and 5."
            );
        }

        return null;
    }

    useEffect(() => {
        if (!isEditing || !hasUnsavedChanges || !task) {
            return;
        }

        const timeout = window.setTimeout(async () => {
            setError(null);

            const validationError = validateFields();

            if (validationError) {
                setError(validationError);
                return;
            }

            setIsSaving(true);

            try {
                await updateTask(projectId, task.id, {
                    title: title.trim(),
                    description: description.trim() || null,
                    estimatedMinutes: estimatedMinutes
                        ? Number(estimatedMinutes)
                        : null,
                    workedMinutes: workedMinutes
                        ? Number(workedMinutes) 
                        : null,
                    deadline: deadline ?? null,
                    priority: priority 
                        ? Number(priority)
                        : null,
                });

                setHasUnsavedChanges(false);
                onSaved();
            } catch (error) {
                console.error(error);

                setError(
                    "Unable to save changes. Please try again."
                );
            } finally {
                setIsSaving(false);
            }
        }, 500);

        return () => {
            window.clearTimeout(timeout);
        };
    }, [
        title, 
        description, 
        estimatedMinutes, 
        workedMinutes, 
        deadline, 
        priority, 
        hasUnsavedChanges, 
        isEditing, 
        projectId, 
        task, 
        onSaved
    ]);

    function markChanged(
        setter: (value: string) => void,
        value: string
    ) {
        setter(value);

        if (isEditing) {
            setHasUnsavedChanges(true);
        }
    }

    return (
        <form 
            className="task-form"
            onSubmit={handleCreate}
        >
            <div className="task-form-header">
                <h3>
                    {isEditing
                        ? title
                        : "New task"}
                </h3>

                {isEditing && (
                    <span 
                        className={`task-save-status ${
                            isSaving
                                ? "saving"
                                : "saved"
                        }`}
                    >
                        {isSaving ? "Saving..." : "Saved"}
                    </span>
                )}
            </div>

            <div className="task-form-field">
                <label htmlFor="task-title">
                    Title
                </label>

                <input 
                    id="task-title"
                    type="text"
                    value={title}
                    onChange={(event) => 
                        markChanged(
                            setTitle,
                            event.target.value
                        )
                    }
                    maxLength={255}
                    required
                />
            </div>

            <div className="task-form-field">
                <label htmlFor="task-description">
                    Description
                </label>

                <textarea 
                    id="task-description"
                    value={description}
                    onChange={(event) => 
                        markChanged(
                            setDescription,
                            event.target.value
                        )
                    }
                    maxLength={2000}
                />
            </div>

            <div className="task-form-field">
                <label htmlFor="task-estimated-minutes">
                    Estimated time
                </label>
                <div className="task-input-with-unit">
                    <input 
                        id="task-estimated-minutes"
                        type="number"
                        min="1"
                        value={estimatedMinutes}
                        onChange={(event) => 
                            markChanged(
                                setEstimatedMinutes,
                                event.target.value
                            )
                        }
                    />
                    <span>min</span>
                </div>
            </div>

            <div className="task-form-field">
                <label htmlFor="task-worked-minutes">
                    Time worked 
                </label>

                <div className="task-input-with-unit">
                    <input 
                        id="task-worked-minutes"
                        type="number"
                        min="0"
                        step="1"
                        value={workedMinutes}
                        onChange={(event) => 
                            markChanged(
                                setWorkedMinutes,
                                event.target.value
                            )
                        }
                    />
                    <span>min</span>
                </div>
            </div>

            <div className="task-form-field">
                <label htmlFor="task-deadline">
                    Deadline
                </label>

                <input 
                    id="task-deadline"
                    type="datetime-local"
                    value={deadline}
                    onChange={(event) =>
                        markChanged(
                            setDeadline,
                            event.target.value
                        )
                    }
                />
            </div>

            <div className="task-form-field">
                <label htmlFor="task-priority">
                    Priority
                </label>

                <select 
                    id="task-priority"
                    value={priority}
                    onChange={(event) =>
                        markChanged(
                            setPriority,
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
                <p 
                    className="task-form-error"
                    role="alert"
                >
                    {error}
                </p>
            )}

            {!isEditing && (
                <div className="task-form-actions">
                    <button
                        type="submit"
                        className="task-primary-button"
                        disabled={isSaving}
                    >
                        {isSaving
                            ? "Creating..."
                            : "Create task"
                        }
                    </button>

                    <button
                        type="button"
                        className="task-secondary-button"
                        onClick={onCancel}
                        disabled={isSaving}
                    >
                        Cancel
                    </button>
                </div>
            )}
        </form>
    );
}