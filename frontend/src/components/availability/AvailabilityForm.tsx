import { useEffect, useState } from "react";
import type { SubmitEvent } from "react";
import type { DayOfWeek } from "../../types/dayOfWeek";
import type {
    Availability, 
    CreateAvailabilityRequest, 
} from "../../types/availability";

interface AvailabilityFormProps {
    availability?: Availability;
    initialDay?: DayOfWeek;
    onSave: (
        request: CreateAvailabilityRequest
    ) => Promise<void>;
    onCancel: () => void;
    isSaving: boolean;
}

const days: DayOfWeek[] = [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY",
    "SATURDAY",
    "SUNDAY",
];

function formatDay(day: DayOfWeek): string {
    return (
        day.charAt(0) + 
        day.slice(1).toLowerCase()
    );
}

export default function AvailabilityForm({
    availability, 
    initialDay,
    onSave, 
    onCancel, 
    isSaving,
}: AvailabilityFormProps) {
    const [dayOfWeek, setDayOfWeek] = 
        useState<DayOfWeek>(
            initialDay ??
            "MONDAY"
        );

    const [startTime, setStartTime] = 
        useState(
            availability?.startTime ?? "09:00"
        );

    const [endTime, setEndTime] = 
        useState(
            availability?.endTime ?? "17:00"
        );
    
    const [error, setError] = 
        useState<string | null>(null);

    useEffect(() => {
        if (availability) {
            setDayOfWeek(availability.dayOfWeek);
        } else if (initialDay) {
            setDayOfWeek(initialDay);
        }

        setStartTime(
            availability?.startTime ?? "09:00"
        );

        setEndTime(
            availability?.endTime ?? "17:00"
        );

        setError(null);
    }, [availability, initialDay]);

    async function handleSubmit(
        event: SubmitEvent
    ) {
        event.preventDefault();

        if (startTime >= endTime) {
            setError(
                "The start time must be before the end time."
            );

            return;
        }

        setError(null);

        await onSave({
            dayOfWeek, 
            startTime, 
            endTime,
        });
    }

    return (
        <form 
            className="availability-form"
            onSubmit={handleSubmit}
        >
            <div className="availability-form-header">
                <h2>
                    {availability
                        ? "Edit availability"
                        : "Add availability"
                    }
                </h2>

                <button 
                    type="button"
                    className="availability-form-close"
                    onClick={onCancel}
                    aria-label="Close"
                >
                    ×
                </button>
            </div>

            <div className="availability-form-fields">
                <div>
                    <label htmlFor="availability-day">
                        Day
                    </label>

                    <select
                        id="availability-day"
                        value={dayOfWeek}
                        onChange={(event) => 
                            setDayOfWeek(
                                event.target.value as DayOfWeek
                            )
                        }   
                    >
                        {days.map((day) => (
                            <option 
                                key={day}
                                value={day}
                            >
                                {formatDay(day)}
                            </option>
                        ))}

                    </select>
                </div>

                <div>
                    <label htmlFor="availability-start">
                        Start
                    </label>

                    <input
                        id="availability-start"
                        type="time"
                        value={startTime}
                        onChange={(event) => 
                            setStartTime(
                                event.target.value
                            )
                        }
                    />
                </div>

                <div>
                    <label htmlFor="availability-end">
                        End
                    </label>

                    <input
                        id="availability-end"
                        type="time"
                        value={endTime}
                        onChange={(event) =>
                            setEndTime(
                                event.target.value
                            )
                        }
                    />
                </div>
            </div>

            {error && (
                <p className="availability-form-error">
                    {error}
                </p>
            )}

            <div className="availability-form-actions">
                <button
                    type="button"
                    onClick={onCancel}
                    disabled={isSaving}
                >
                    Cancel
                </button>

                <button 
                    type="submit"
                    disabled={isSaving}
                >
                    {isSaving
                        ? "Saving..."
                        : "Save"
                    }
                </button>
            </div>
        </form>
    );
}