import type { Availability } from "../../types/availability";
import type { DayOfWeek } from "../../types/dayOfWeek";

interface AvailabilityListProps {
    availability: Availability[];
    onAdd: (dayOfWeek : DayOfWeek) => void;
    onEdit: (availability: Availability) => void;
    onDelete: (availability: Availability) => void;
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

function formatTime(time: string): string {
    const [hours, minutes] = 
        time.split(":").map(Number);

    const date = new Date();

    date.setHours(hours, minutes, 0, 0);

    return date.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function AvailabilityList({
    availability, 
    onAdd, 
    onEdit, 
    onDelete,
}: AvailabilityListProps) {
    return (
        <div className="availability-list">
            {days.map((day) => {
                const dayAvailability = 
                    availability
                        .filter(
                            (item) => 
                                    item.dayOfWeek === day
                        )
                        .sort((a, b) => 
                            a.startTime.localeCompare(
                                b.startTime
                            )
                        );
                
                return (
                    <section 
                        key={day}
                        className="availability-day"
                    >
                        <div className="availability-day-header">
                            <h2>
                                {formatDay(day)}
                            </h2>

                            <button
                                type="button"
                                onClick={() => 
                                    onAdd(day)
                                }
                            >
                                + Add
                            </button>
                        </div>

                        {dayAvailability.length === 
                        0 ? (
                            <p className="availability-day-empty">
                                No availability
                            </p>
                        ) : (
                            <div className="availability-windows">
                                {dayAvailability.map(
                                    (item) => (
                                        <div
                                            key={item.id}
                                            className="availability-window"
                                            onClick={() => 
                                                onEdit(item)
                                            }
                                        >
                                            <span>
                                                {formatTime(
                                                    item.startTime
                                                )}
                                            </span>

                                            <span className="availability-window-line" />

                                            <span>
                                                {formatTime(
                                                    item.endTime
                                                )}
                                            </span>

                                            <button
                                                type="button"
                                                className="availability-delete"
                                                onClick={(event) => {
                                                    event.stopPropagation();

                                                    onDelete(
                                                        item
                                                    );
                                                }}
                                                aria-label={`Delete ${formatDay(day)} availability`}
                                            >
                                                ×
                                            </button>
                                        </div>
                                    )
                                )}
                            </div>
                        )
                        }
                    </section>
                );
            })}
        </div>
    );
}