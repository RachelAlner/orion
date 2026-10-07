import { useEffect, useState } from "react";
import type { SubmitEvent } from "react";

import { createProject, updateProject } from "../../services/projectService";
import type { Project } from "../../types/project";

interface ProjectFormProps {
    project?: Project;
    onSaved: () => void;
    onCancel: () => void;
}

export default function ProjectForm({
    project,
    onSaved,
    onCancel, 
}: ProjectFormProps) {
    const isEditing = project !== undefined;

    const [name, setName] = useState(
        project?.name ?? ""
    );
    const [description, setDescription] = useState(
        project?.description ?? ""
    );
    const [deadline, setDeadline] = useState(
        project?.deadline
        ? project.deadline.slice(0,16)
        : ""
    );
    const [priority, setPriority] = useState(
        project?.priority?.toString() ?? ""
    );

    const [error, setError] = useState<string | null>(null);
    
    const [isSaving, setIsSaving] = useState(false);
    const [hasUnsavedChanges, setHasUnsavedChanges] = 
        useState(false);

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
            await createProject({
                name: name.trim(),
                description: description.trim() || null,
                deadline: deadline ?? null,
                priority: priority 
                    ? Number(priority)
                    : null,
            });

            onSaved();
        } catch (error) {
            console.error(error);

            setError(
                "Unable to create the project. Please try again."
            );
        } finally {
            setIsSaving(false);
        }
    }

    function validateFields(): string | null {
        const trimmedName = name.trim();

        if (!trimmedName) {
            return "Project title is required.";
        }

        if (trimmedName.length > 255) {
            return (
                "Project title must be 255 characters or fewer."
            );
        }

        if (description.length > 2000) {
            return (
                "Description must be 2,000 characters or fewer."
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
            return (
                "Priority must be between 1 and 5."
            );
        }

        return null;
    }

    useEffect(() => {
        if (
            !isEditing || 
            !hasUnsavedChanges || 
            !project
        ) {
            return;
        }

        const timeout = window.setTimeout(
            async () => {
                setError(null);

                const validationError = 
                    validateFields();
                
                if (validationError) {
                    setError(validationError);
                    return;
                }

                setIsSaving(true);

                try {
                    await updateProject(
                        project.id, 
                        {
                            name: name.trim(),
                            description: 
                                description.trim() || null,
                            deadline: deadline ?? null,
                            priority: priority
                                ? Number(priority)
                                : null,
                        }
                    );

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
            }, 500
        );

        return () => {
            window.clearTimeout(timeout);
        };
    }, [
        name, 
        description, 
        deadline, 
        priority, 
        hasUnsavedChanges, 
        isEditing, 
        project,
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
            onSubmit={handleCreate}
            className="project-form"
        >
            <div className="project-form-header">
                <div>
                    <h2> 
                        {isEditing
                            ? name
                            : "New project"
                        }
                    </h2>

                    <p>
                        {isEditing
                            ? "Changes are saved automatically."
                            : "Create a project to organise your work."
                        }
                    </p>   
                </div>

                {isEditing && (
                    <span
                        className={`project-save-status ${
                            isSaving
                                ? "saving"
                                : "saved"
                        }`}
                    >
                        {isSaving
                            ? "Saving..."
                            : "Saved"
                        }
                    </span>
                )} 
            </div>

            <div className="project-form-field">
                <label htmlFor="project-name">
                    Title
                </label>

                <input
                    id="project-name"
                    type="text"
                    value={name}
                    onChange={(event) =>
                        markChanged(
                            setName,
                            event.target.value
                        )
                    }
                    maxLength={255}
                    required
                />
            </div>

            <div className="project-form-field">
                <label htmlFor="project-description">
                    Description
                </label>

                <textarea 
                    id="project-description"
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

            <div className="project-form-field">
                <label htmlFor="project-deadline">
                    Deadline
                </label>

                <input 
                    id="project-deadline"
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

            <div className="project-form-field">
                <label htmlFor="project-priority">
                    Priority
                </label>

                <select 
                    id="project-priority"
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

                    <option value="1">1 - Lowest</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5 - Highest</option>
                </select>
            </div>

            {error && (
                <p 
                    className="project-form-error"
                    role="alert"
                >
                    {error}
                </p>
            )}

            {!isEditing && (
                <div className="project-form-actions">
                    <button 
                        type="submit"
                        className="project-primary-button"
                        disabled={isSaving}
                    >
                        {isSaving
                            ? "Creating..."
                            : "Create project"
                        }
                    </button>

                    <button 
                        type="button"
                        className="project-secondary-button"
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