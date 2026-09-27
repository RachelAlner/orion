import { useState } from "react";
import type { SubmitEvent } from "react";

import { createProject, updateProject } from "../services/projectService";
import type { Project } from "../types/project";

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

    const [error, setError] = useState<String | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(
        event: SubmitEvent
    ) {
        event.preventDefault();

        setError(null);

        const trimmedName = name.trim();

        if (!trimmedName) {
            setError("Project name is required.");
            return;
        }

        if (trimmedName.length > 255) {
            setError(
                "Project name must be 255 characters or fewer."
            );
            return;
        }

        if (description.length > 2000) {
            setError(
                "Description must be 2,000 characters or fewer."
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
                name: trimmedName, 
                description: 
                    description.trim() || null, 
                deadline: deadline || null, 
                priority: priorityValue
            };

            if (isEditing) {
                await updateProject(
                    project.id, 
                    request
                );
            } else {
                await createProject(request);
            }

            onSaved();
        } catch (error) {
            console.error(error);

            setError(
                isEditing
                    ? "Unable to update the project. Please try again."
                    : "Unable to create the project. Please try again."
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2> 
                {isEditing
                    ? "Edit project"
                    : "New project"
                }
            </h2>

            <div>
                <label htmlFor="project-name">
                    Name 
                </label>

                <input
                    id="project-name"
                    type="text"
                    value={name}
                    onChange={(event) =>
                        setName(event.target.value)
                    }
                    maxLength={200}
                    required
                />
            </div>

            <div>
                <label htmlFor="project-description">
                    Description
                </label>

                <textarea 
                    id="project-description"
                    value={description}
                    onChange={(event) =>
                        setDescription(event.target.value)
                    }
                    maxLength={2000}
                />
            </div>

            <div>
                <label htmlFor="project-deadline">
                    Deadline
                </label>

                <input 
                    id="project-deadline"
                    type="datetime-local"
                    value={deadline}
                    onChange={(event) => 
                        setDeadline(event.target.value)
                    }
                />
            </div>

            <div>
                <label htmlFor="project-priority">
                    Priority
                </label>

                <select 
                    id="project-priority"
                    value={priority}
                    onChange={(event) =>
                        setPriority(event.target.value)
                    }
                >
                    <option value="">
                        No priority
                    </option>

                    <option value="1">1 - Lowest</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="4">5 - Highest</option>
                </select>
            </div>

            {error && (
                <p role="aler">
                    {error}
                </p>
            )}

            <button 
                type="submit"
                disabled={isSubmitting}
            >
                {isSubmitting
                    ? isEditing 
                        ? "Saving..."
                        : "Creating..."
                    : isEditing 
                        ? "Save changes"
                        : "Create project"
                }
            </button>

            <button 
                type="button"
                onClick={onCancel}
                disabled={isSubmitting}
            >
                Cancel
            </button>
        </form>
    );
}