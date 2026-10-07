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

interface Duration {
    years: string;
    months: string;
    weeks: string;
    days: string;
    hours: string;
    minutes: string;
}

const MINUTES_PER_HOUR = 60;
const MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;
const MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY;
const MINUTES_PER_MONTH = 30 * MINUTES_PER_DAY;
const MINUTES_PER_YEAR = 365 * MINUTES_PER_DAY;

function emptyDuration(): Duration {
    return {
        years: "",
        months: "",
        weeks: "",
        days: "",
        hours: "",
        minutes: "",
    };
}

function minutesToDuration(totalMinutes: number | null): Duration {
    if (totalMinutes === null) {
        return emptyDuration();
    }

    let remaining = totalMinutes;

    const years = Math.floor(
        remaining / MINUTES_PER_YEAR
    );
    remaining %= MINUTES_PER_YEAR;

    const months = Math.floor(
        remaining / MINUTES_PER_MONTH
    );
    remaining %= MINUTES_PER_MONTH;

    const weeks = Math.floor(
        remaining / MINUTES_PER_WEEK
    );
    remaining %= MINUTES_PER_WEEK;

    const days = Math.floor(
        remaining / MINUTES_PER_DAY
    );
    remaining %= MINUTES_PER_DAY;

    const hours = Math.floor(
        remaining / MINUTES_PER_HOUR
    );
    remaining %= MINUTES_PER_HOUR;

    const minutes = remaining;

    return {
        years: years > 0 ? years.toString() : "",
        months: months > 0 ? months.toString() : "",
        weeks: weeks > 0 ? weeks.toString() : "",
        days: days > 0 ? days.toString() : "",
        hours: hours > 0 ? hours.toString() : "",
        minutes: minutes > 0 ? minutes.toString() : "",
    };
}

function durationToMinutes(duration: Duration): number | null {
    const values = {
        years: duration.years 
            ? Number(duration.years)
            : 0,
        months: duration.months
            ? Number(duration.months)
            : 0,
        weeks: duration.weeks
            ? Number(duration.weeks)
            : 0,
        days: duration.days
            ? Number(duration.days)
            : 0,
        hours: duration.hours
            ? Number(duration.hours)
            : 0,
        minutes: duration.minutes
            ? Number(duration.minutes)
            : 0,
    }; 

    const hasValue = Object.values(values).some(
        (value) => value > 0
    );

    if (!hasValue) {
        return null;
    }

    return (
        values.years * MINUTES_PER_YEAR + 
        values.months * MINUTES_PER_MONTH + 
        values.weeks * MINUTES_PER_WEEK + 
        values.days * MINUTES_PER_DAY + 
        values.hours * MINUTES_PER_HOUR + 
        values.minutes
    );
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

    const [estimatedTime, setEstimatedTime] = 
        useState<Duration>(
            minutesToDuration(
                task?.estimatedMinutes ?? null
            )
        );

    const [workedTime, setWorkedTime] = useState<Duration>(
        minutesToDuration(
            task?.workedMinutes ?? null 
        )
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

    function updateEstimatedTime(
        field: keyof Duration,
        value: string
    ) {
        setEstimatedTime((current) => ({
            ...current, 
            [field]: value,
        }));

        if (isEditing) {
            setHasUnsavedChanges(true);
        }
    }

    function updateWorkedTime(
        field: keyof Duration,
        value: string
    ) {
        setWorkedTime((current) => ({
            ...current, 
            [field]: value,
        }));

        if (isEditing) {
            setHasUnsavedChanges(true);
        }
    }

    function markChanged(
        setter: (value: string) => void,
        value: string
    ) {
        setter(value);

        if (isEditing) {
            setHasUnsavedChanges(true);
        }
    }

    function validateDuration(
        duration: Duration,
        label: string, 
        allowEmpty: boolean 
    ): string | null {
        const fields: Array<
            [keyof Duration, string]
        > = [
            ["years", "Years"],
            ["months", "Months"],
            ["weeks", "Weeks"],
            ["days", "Days"],
            ["hours", "Hours"],
            ["minutes", "Minutes"],
        ];

        let totalMinutes = 0;

        for (const [field, fieldLabel] of fields) {
            const value = duration[field];

            if (!value) {
                continue;
            }

            const numberValue = Number(value);

            if (
                !Number.isInteger(numberValue) ||
                numberValue < 0
            ) {
                return `${label} ${fieldLabel.toLowerCase()} must be a whole number.`;
            }
        }

        totalMinutes = durationToMinutes(duration) ?? 0;

        if (!allowEmpty && totalMinutes < 1) {
            return `${label} must be at least 1 minute.`;
        }

        return null;
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
            durationToMinutes(estimatedTime);
            
        const estimatedError = validateDuration(
            estimatedTime, 
            "Estimated time",
            true
        );

        if (estimatedError) {
            return estimatedError;
        }

        const workedMinutesValue =
            durationToMinutes(workedTime) ?? 0;

        if (isEditing) {
            const workedError = validateDuration(
                workedTime, 
                "Time worked",
                true
            );

            if (workedError) {
                return workedError;
            }

            if (
                estimatedMinutesValue !== null && 
                workedMinutesValue > estimatedMinutesValue
            ) {
                return (
                    "Time worked cannot exceed estimated time."
                );
            }
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
                estimatedMinutes: 
                    durationToMinutes(estimatedTime),
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
                    estimatedMinutes: 
                        durationToMinutes(estimatedTime),
                    workedMinutes: 
                        durationToMinutes(workedTime) ?? 0,
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
        estimatedTime, 
        workedTime, 
        deadline, 
        priority, 
        hasUnsavedChanges, 
        isEditing, 
        projectId, 
        task, 
        onSaved
    ]);

    function renderDurationFields(
        duration: Duration,
        updateDuration: (
            field: keyof Duration,
            value: string 
        ) => void, 
        prefix: string
    ) {
        const fields: Array<
            [keyof Duration, string]
        > = [
            ["years", "Years"],
            ["months", "Months"],
            ["weeks", "Weeks"],
            ["days", "Days"],
            ["hours", "Hours"],
            ["minutes", "Minutes"],
        ];

        return (
            <div className="task-duration-fields">
                {fields.map(
                    ([id, label]) => (
                        <div 
                            className="task-duration-field"
                            key={id}
                        > 
                            <label
                                htmlFor={`${prefix}-${id}`}
                            >
                                {label}
                            </label>

                            <input 
                                id={`${prefix}-${id}`}
                                type="number"
                                min="0"
                                step="1"
                                value={duration[id]}
                                onChange={(event) => 
                                    updateDuration(
                                        id, 
                                        event.target.value
                                    )
                                }
                            />
                        </div>
                    )
                )}
            </div>
        );
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

            <div className="task-form-field">
                <label>
                    Estimated time
                </label>
                
                {renderDurationFields(
                    estimatedTime, 
                    updateEstimatedTime, 
                    "task-estimated"
                )}
            </div>

            {isEditing && (
                <div className="task-form-field">
                    <label>
                        Time worked 
                    </label>

                    {renderDurationFields(
                        workedTime, 
                        updateWorkedTime,
                        "task-worked"
                    )}
                </div>
            )}

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