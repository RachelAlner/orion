import { useState, useEffect } from "react";
import type { Task } from "../types/task";
import type { ScheduleResponse } from "../types/schedule";
import { getTasks } from "../services/taskService";
import { getProjects } from "../services/projectService";
import { generateSchedule } from "../services/scheduleService";
import ScheduleBlockList from "../components/schedule/ScheduleBlockList";
import UnscheduledTaskList from "../components/schedule/UnscheduledTaskList";

function getStartOfWeek(): Date {
    const date = new Date();

    const day = date.getDay();

    const difference = 
        day === 0
            ? -6 
            : 1 - day;
    
    date.setDate(date.getDate() + difference);
    date.setHours(0, 0, 0, 0);

    return date;
}

function getEndOfWeek(): Date {
    const start = getStartOfWeek();

    const end = new Date(start);
    end.setDate(end.getDate() + 7);

    return end;
}

function toLocalDateTimeValue(
    date: Date
): string {
    const year = date.getFullYear();
    const month = String(
        date.getMonth() + 1
    ).padStart(2, "0");
    const day = String(
        date.getDate()
    ).padStart(2, "0");
    const hours = String(
        date.getHours()
    ).padStart(2, "0");
    const minutes = String(
        date.getMinutes()
    ).padStart(2, "0");

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function formatDateInputValue(
    date: Date 
): string {
    const year = date.getFullYear();
    const month = String(
        date.getMonth() + 1
    ).padStart(2, "0");
    const day = String(
        date.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

export default function SchedulePage() {
    const [tasks, setTasks] = 
        useState<Task[]>([]);

    const [isLoadingTasks, setIsLoadingTasks] = 
        useState(true);

    const [periodStart, setPeriodStart] = useState(
        formatDateInputValue(
            getStartOfWeek()
        )
    );

    const [periodEnd, setPeriodEnd] = useState(
        formatDateInputValue(
            getEndOfWeek()
        )
    );

    const [schedule, setSchedule] = 
        useState<ScheduleResponse | null>(null);

    const [isGenerating, setIsGenerating] = useState(false);

    const [error, setError] = 
        useState<string | null>(null);
    
    useEffect(() => {
        async function loadTasks() {
            try {
                setIsLoadingTasks(true);
                setError(null);

                const projects = await getProjects();

                const projectTasks = 
                    await Promise.all(
                        projects.map((project) =>
                            getTasks(project.id)
                        )
                    );
                
                const allTasks = 
                    projectTasks.flat();
                    
                setTasks(allTasks);
            } catch (error) {
                console.error(error);

                setError(
                    "Unable to load your tasks."
                );
            } finally {
                setIsLoadingTasks(false);
            }
        }

        loadTasks();
    }, []);

    async function handleGenerateSchedule() {
        try {
            setError(null);

            const start = new Date(
                `${periodStart}T00:00:00`
            );

            const end = new Date(
                `${periodEnd}T00:00:00`
            );

            if (start >= end) {
                setError(
                    "The schedule start date must be before the end date."
                );
                return;
            }

            setIsGenerating(true);

            const result = 
                await generateSchedule(
                    toLocalDateTimeValue(start),
                    toLocalDateTimeValue(end)
                );
            
            setSchedule(result);
        } catch (error) {
            console.error(error);

            setError(
                "Unable to generate the schedule."
            );
        } finally {
            setIsGenerating(false);
        }
    }

    return (
        <div className="schedule-page">
            <div className="schedule-page-header">
                <div>
                    <h1>Schedule</h1>
                    <p>
                        Plan your available time around 
                        your current tasks.
                    </p>
                </div>

                <button 
                    type="button"
                    onClick={handleGenerateSchedule}
                    disabled={
                        isGenerating || 
                        isLoadingTasks
                    }
                    className="schedule-generate-button"
                >
                    {isGenerating
                        ? "Generating..."
                        : "Generate schedule"
                    }
                </button>
            </div>

            <section className="schedule-period">
                <div>
                    <label htmlFor="schedule-start">
                        Start
                    </label>

                    <input 
                        id="schedule-start"
                        type="date"
                        value={periodStart}
                        onChange={(event) =>
                            setPeriodStart(
                                event.target.value
                            )
                        }
                    />
                </div>

                <div>
                    <label htmlFor="schedule-end">
                        End
                    </label>

                    <input 
                        id="schedule-end"
                        type="date"
                        value={periodEnd}
                        onChange={(event) => 
                            setPeriodEnd(
                                event.target.value
                            )
                        }
                    />
                </div>
            </section>

            {isLoadingTasks && (
                <p className="schedule-empty-page">
                    Loading tasks...
                </p>
            )}

            {error && (
                <p className="schedule-error">
                    {error}
                </p>
            )}

            {!isLoadingTasks && 
                !schedule && 
                !error && (
                    <p className="schedule-empty-page">
                        Generate a schedule to see 
                        your planned work.
                    </p>
                )}

            {schedule && (
                <>
                    <ScheduleBlockList
                        blocks={
                            schedule.scheduledBlocks
                        }
                        tasks={tasks}
                    />

                    <UnscheduledTaskList
                        tasks={tasks}
                        unscheduledTasks={
                            schedule.unscheduledTasks
                        }
                    />
                </>
            )}
        </div>
    );
}