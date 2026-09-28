import { useState } from "react";
import type { SubmitEvent } from "react";

import { updateTaskProgress } from "../services/taskService";
import type { Task } from "../types/task";

interface ProgressFormProps {
    projectId: string;
    task: Task;
    onSaved: (task: Task) => void;
    onCancel: () => void;
}

export default function ProgressForm({
    projectId, 
    task, 
    onSaved,
    onCancel,
}: ProgressFormProps ) {
    const [workedMinutes, setWorkedMinutes] = useState(
        String(task.workedMinutes)
    );
    const [error, setError] = 
        useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = 
        useState(false);
    
    async function handleSubmit(
        event: SubmitEvent
    ) {
        event.preventDefault();

        setError(null);

        const minutes = Number(workedMinutes);

        if (
            !Number.isInteger(minutes) ||
            minutes < 0
        ) {
            setError(
                "Minutes worked must be a whole number."
            );
            return;
        }

        setIsSubmitting(true);

        try {
            const updatedTask = await updateTaskProgress(
                projectId, 
                task.id, 
                {
                    workedMinutes: minutes,
                }
            );

            onSaved(updatedTask);
        } catch (error) {
            console.error(error);

            setError(
                "Unable to record progress. Please try again."
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h3>Update progress</h3>

            <p>
                <strong>{task.title}</strong>
            </p>
            <div>
                <p>
                    Estimated: {" "}
                    {task.estimatedMinutes !== null 
                        ? `${task.estimatedMinutes} minutes`
                        : "No estimate"}
                </p>

                <p>
                    Worked: {task.workedMinutes} minutes
                </p>

                <p>
                    Remaining:{" "}
                    {task.remainingMinutes !== null 
                        ? `${task.remainingMinutes} minutes`
                        : "No estimate"
                    }
                </p>
            </div>

            <div>
                <label htmlFor="worked-minutes">
                    Total time worked
                </label>

                <input 
                    id="worked-minutes"
                    type="number"
                    min="0"
                    step="1"
                    value={workedMinutes}
                    onChange={(event) =>
                        setWorkedMinutes(
                            event.target.value
                        )
                    }
                    required 
                />
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
                        ? "Saving..."
                        : "Save progress"}
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