import { useState, useEffect } from "react";
import type { Task } from "../types/task";
import type { ScheduleResponse } from "../types/schedule";
import { getTasks } from "../services/taskService";
import { getProjects } from "../services/projectService";
import { generateSchedule, getCurrentSchedule } from "../services/scheduleService";
import { ApiError } from "../services/api";
import ScheduleBlockList from "../components/schedule/ScheduleBlockList";
import UnscheduledTaskList from "../components/schedule/UnscheduledTaskList";
import styles from "./SchedulePage.module.css";

function formatPeriodDate(value: string): string {
    return new Date(value).toLocaleDateString([], {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}


export default function SchedulePage() {
    const [tasks, setTasks] = 
        useState<Task[]>([]);

    const [isLoadingTasks, setIsLoadingTasks] = 
        useState(true);

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
                
                    
                setTasks(projectTasks.flat());
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

    useEffect(() => {
        async function loadCurrentSchedule() {
            try {
                const result = await getCurrentSchedule();
                setSchedule(result);
            } catch (error) {
                if (error instanceof ApiError && error.status === 404) {
                    return;
                }

                console.error(error);
                setError("Unable to load your current schedule.")
            }   
            
        }

        loadCurrentSchedule();
    }, []);

    async function handleGenerateSchedule() {
        try {
            setError(null);

            setIsGenerating(true);

            const result = 
                await generateSchedule();
            
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
        <div className={styles.schedulePage}>
            <div className={styles.schedulePageHeader}>
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
                    className={styles.scheduleGenerateButton}
                >
                    {isGenerating
                        ? "Generating..."
                        : "Generate schedule"
                    }
                </button>
            </div>

            {isLoadingTasks && (
                <p className={styles.scheduleEmptyPage}>
                    Loading tasks...
                </p>
            )}

            {error && (
                <p className={styles.scheduleError}>
                    {error}
                </p>
            )}

            {!isLoadingTasks &&
                !schedule &&
                !error && (
                    <p className={styles.scheduleEmptyPage}>
                        Generate a schedule to see
                        your planned work.
                    </p>
                )}

            {schedule && (
                <>
                    <p>
                        Schedule period:{" "}
                        {formatPeriodDate(schedule.periodStart)}
                        {" - "}
                        {formatPeriodDate(schedule.periodEnd)}
                    </p>

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