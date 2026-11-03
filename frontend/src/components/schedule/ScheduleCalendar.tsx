import { useEffect, useRef, useState } from "react";
import type { ScheduleResponse } from "../../types/schedule";
import type { Task } from "../../types/task";
import styles from "./ScheduleCalendar.module.css";

interface ScheduleCalendarProps {
    schedule: ScheduleResponse;
    tasks: Task[];
}

const START_HOUR = 6;
const END_HOUR = 23;

function parseLocalDateTime(value: string): Date {
    const [datePart, timePart] = value.split("T");
    const [year, month, day] = datePart.split("-").map(Number);
    const [hour, minute, second = 0] = timePart.split(":").map(Number);

    return new Date(year, month - 1, day, hour, minute, second);
}

function startOfWeek(date: Date): Date {
    const result = new Date(date);
    const day = result.getDay();
    const difference = day === 0 ? -6 : 1 - day;

    result.setDate(result.getDate() + difference);
    result.setHours(0, 0, 0, 0);

    return result;
}

function addDays(date: Date, amount: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + amount);
    return result;
}

function formatDay(date: Date): string {
    return date.toLocaleDateString("en-GB", {
        weekday: "short",
        day: "numeric",
    });
}

function formatTime(date: Date): string {
    return date.toLocaleTimeString("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
    });
}

function formatWeek(date: Date): string {
    return date.toLocaleDateString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
    });
}

export default function ScheduleCalendar({
    schedule, 
    tasks, 
}: ScheduleCalendarProps) {
    const calendarRef = useRef<HTMLDivElement>(null);
    const [calendarHeight, setCalendarHeight] = useState(0);

    useEffect(() => {
        const element = calendarRef.current;

        if (!element) return;

        const observer = new ResizeObserver(([entry]) => {
            setCalendarHeight(entry.contentRect.height);
        });

        observer.observe(element);

        return () => observer.disconnect();
    }, []);

    const HEADER_HEIGHT = 48;

    const HOUR_HEIGHT = Math.max(
        0, 
        (calendarHeight - HEADER_HEIGHT) / (END_HOUR - START_HOUR)
    );

    const [weekOffset, setWeekOffset] = useState(0);

    const initialDate = parseLocalDateTime(schedule.periodStart);
    const weekStart = addDays(startOfWeek(initialDate), weekOffset * 7);

    const days = Array.from({ length: 7 }, (_, index) => 
        addDays(weekStart, index)
    );

    const taskTitles = new Map(
        tasks.map((task) => [task.id, task.title])
    );

    const hours = Array.from(
        { length: END_HOUR - START_HOUR },
        (_, index) => START_HOUR + index
    );

    const weekEnd = addDays(weekStart, 7);

    const blocks = schedule.scheduledBlocks
        .map((block) => ({
            ...block, 
            start: parseLocalDateTime(block.startTime),
            end: parseLocalDateTime(block.endTime),
        }))
        .filter(
            (block) => 
                block.start < weekEnd && 
                block.end > weekStart
        );

    function getBlockStyle(start: Date, end: Date) {
        const visibleStart = new Date(start);
        const visibleEnd = new Date(end);

        const dayStart = new Date(start);
        dayStart.setHours(START_HOUR, 0, 0, 0);

        const dayEnd = new Date(start);
        dayEnd.setHours(END_HOUR, 0, 0, 0);

        const clippedStart = 
            visibleStart < dayStart ? dayStart : visibleStart;

        const clippedEnd = 
            visibleEnd > dayEnd ? dayEnd : visibleEnd;

        const startMinutes = 
            clippedStart.getHours() * 60 + clippedStart.getMinutes()
            - START_HOUR * 60;

        const durationMinutes = 
            (clippedEnd.getTime() - clippedStart.getTime()) / 60000;

        return {
            top: `${(startMinutes / 60) * HOUR_HEIGHT}px`,
            height: `${Math.max(
                (durationMinutes / 60) * HOUR_HEIGHT,
                24
            )}px`,
        };
    }

    return (
        <section className={styles.scheduleCalendar}>
            <div className={styles.calendarHeader}>
                <button
                    type="button"
                    onClick={() => setWeekOffset((offset) => offset - 1)}
                >
                    Previous
                </button>

                <h2>
                    Week of {formatWeek(weekStart)}
                </h2>

                <button
                    type="button"
                    onClick={() => setWeekOffset((offset) => offset + 1)}
                >
                    Next
                </button>
            </div>

            <div 
                className={styles.calendarScroll}
                ref={calendarRef}
            >
                <div className={styles.calendarGrid}>
                    <div className={styles.calendarCorner} />

                    {days.map((day) => (
                        <div className={styles.calendarDayHeader} key={day.toISOString()}>
                            {formatDay(day)}
                        </div>
                    ))}

                    <div className={styles.calendarTimeColumn}>
                        {hours.map((hour) => (
                            <div
                                className={styles.calendarHourLabel}
                                style={{ height: `${HOUR_HEIGHT}px` }}
                                key={hour}
                            >
                                {`${String(hour).padStart(2, "0")}:00`}
                            </div>
                        ))}
                    </div>

                    {days.map((day) => {
                        const dayStart = new Date(day);
                        dayStart.setHours(0, 0, 0, 0);

                        const nextDay = addDays(dayStart, 1);

                        const dayBlocks = blocks.filter(
                            (block) => 
                                block.start < nextDay && 
                                block.end > dayStart
                        );

                        return (
                            <div
                                className={styles.calendarDayColumn}
                                style={{ height: `${HOUR_HEIGHT * (END_HOUR - START_HOUR)}px` }}
                                key={day.toISOString()}
                            >
                                {hours.map((hour) => (
                                    <div
                                        className={styles.calendarHourCell}
                                        style={{ height: `${HOUR_HEIGHT}px`}}
                                        key={hour}
                                    />
                                ))}

                                {dayBlocks.map((block) => (
                                    <div
                                        className={styles.calendarTask}
                                        key={`${block.taskId}-${block.startTime}`}
                                        style={getBlockStyle(
                                            block.start,
                                            block.end
                                        )}
                                        title={`${taskTitles.get(block.taskId) ?? "Task"}: ${formatTime(block.start)}-${formatTime(block.end)}`}
                                    > 
                                        <strong>
                                            {taskTitles.get(block.taskId) ?? "Task"}
                                        </strong>

                                        <span>
                                            {formatTime(block.start)}-
                                            {formatTime(block.end)}
                                        </span>
                                    </div>
                                ))}

                            </div>
                        );
                    })}
                </div>
            </div>
        </section>
    );
}