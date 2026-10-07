import type { Task } from "../../types/task";
import type { ScheduleBlock } from "../../types/schedule";

interface ScheduleBlockListProps {
    blocks: ScheduleBlock[];
    tasks: Task[];
}

function formatTime(value: string): string {
    return new Date(value).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
    });
}

function formatDate(value: string): string {
    return new Date(value).toLocaleDateString([], {
        weekday: "long",
        day: "numeric",
        month: "long",
    });
}

function formatDuration(
    startTime: string, 
    endTime: string
): string {
    const start = new Date(startTime).getTime();
    const end = new Date(endTime).getTime();

    const minutes = Math.round((end - start) / 60000);

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (hours === 0) {
        return `${remainingMinutes} min`;
    }

    if (remainingMinutes === 0) {
        return `${hours}h`;
    }

    return `${hours}h ${remainingMinutes}m`;
}

export default function ScheduleBlockList({
    blocks, 
    tasks,
}: ScheduleBlockListProps) {
    if (blocks.length === 0) {
        return (
            <section className="schedule-section">
                <div className="schedule-section-header">
                    <h2>Schedule</h2>
                </div>

                <p className="schedule-empty">
                    No work was scheduled for this period.
                </p>
            </section>
        );
    }

    const sortedBlocks = [...blocks].sort(
        (a, b) => 
            new Date(a.startTime).getTime() - 
            new Date(b.startTime).getTime()
    );

    let previousDate = "";

    return (
        <section className="schedule-section">
            <div className="schedule-section-header">
                <h2>Schedule</h2>
                <span>{blocks.length}</span>
            </div>

            <div className="schedule-block-list">
                {sortedBlocks.map((block) => {
                    const task = tasks.find(
                        (candidate) => 
                            candidate.id === block.taskId
                    );

                    const date = new Date(
                        block.startTime
                    ).toDateString();

                    const showDate = date !== previousDate;
                    previousDate = date;

                    return (
                        <div key={`${block.taskId}-${block.startTime}`}>
                            {showDate && (
                                <div className="schedule-date">
                                    {formatDate(block.startTime)}
                                </div>
                            )}

                            <div className="schedule-block">
                                <div className="schedule-block-time">
                                    <span>
                                        {formatTime(block.startTime)}
                                    </span>
                                    <span>
                                        {formatTime(block.endTime)}
                                    </span>
                                </div>

                                <div className="schedule-block-content">
                                    <strong>
                                        {task?.title ??
                                            "Unknown task"}
                                    </strong>

                                    <span>
                                        {formatDuration(
                                            block.startTime,
                                            block.endTime
                                        )}
                                    </span>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </section>
    );
}