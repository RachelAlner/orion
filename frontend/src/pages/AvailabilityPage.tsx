import { useEffect, useState } from "react";
import type { Availability } from "../types/availability";
import type { DayOfWeek } from "../types/dayOfWeek";
import {
    createAvailability,
    deleteAvailability,
    getAvailability,
    updateAvailability,
} from "../services/availabilityService";
import AvailabilityForm from "../components/availability/AvailabilityForm";
import AvailabilityList from "../components/availability/AvailabilityList";

export default function AvailabilityPage() {
    const [availability, setAvailability] = 
        useState<Availability[]>([]);
    
    const [isLoading, setIsLoading] = 
        useState(true);

    const [error, setError] = 
        useState<string | null>(null);
    
    const [editingAvailability, setEditingAvailability] = 
        useState<Availability | null>(null);
    
    const [addingDay, setAddingDay] = 
        useState<DayOfWeek | null>(null);

    const [isSaving, setIsSaving] = 
        useState(false);
    
    useEffect(() => {
        loadAvailability();
    }, []);

    async function loadAvailability() {
        try {
            setIsLoading(true);
            setError(null);

            const result = 
                await getAvailability();
            
            setAvailability(result);
        } catch (error) {
            console.error(error);

            setError(
                "Unable to load your availability."
            );
        } finally {
            setIsLoading(false);
        }
        
    }

    function handleAdd(dayOfWeek: DayOfWeek) {
        setEditingAvailability(null);
        setAddingDay(dayOfWeek);
    }

    function handleEdit(
        item: Availability
    ) {
        setAddingDay(null);
        setEditingAvailability(item);
    }

    function handleCancel() {
        setAddingDay(null);
        setEditingAvailability(null);
    }

    async function handleSave(
        request: {
            dayOfWeek: DayOfWeek;
            startTime: string;
            endTime: string;
        }
    ) {
        try {
            setIsSaving(true);
            setError(null);

            if (editingAvailability) {
                const updated = 
                    await updateAvailability(
                        editingAvailability.id, 
                        request
                    );
                
                setAvailability((current) => 
                    current.map((item) =>
                        item.id === 
                        updated.id 
                            ? updated 
                            : item
                    )
                );
            } else {
                const created = 
                    await createAvailability(
                        request
                    );
                
                setAvailability((current) => [
                    ...current, 
                    created,
                ]);
            }

            handleCancel();
        } catch (error) {
            console.error(error);

            setError(
                "Unable to save availability."
            )
        } finally {
            setIsSaving(false);
        }
    }

    async function handleDelete(
        item: Availability
    ) {
        try {
            setError(null);

            await deleteAvailability(
                item.id
            );

            setAvailability((current) =>
                current.filter(
                    (availabilityItem) => 
                        availabilityItem.id !==
                        item.id
                )
            );

            if (
                editingAvailability?.id === 
                item.id
            ) {
                handleCancel();
            }
        } catch (error) {
            console.error(error);

            setError(
                "Unable to delete availability."
            );
        }
    }

    return (
        <div className="availability-page">
            <div className="availability-page-header">
                <div>
                    <h1>Availability</h1>

                    <p>
                        Set the times when you are 
                        available to work.
                    </p>
                </div>
            </div>

            {error && (
                <p className="availability-error">
                    {error}
                </p>
            )}

            {isLoading ? (
                <p className="availability-empty-page">
                    Loading availability...
                </p>
            ) : (
                <AvailabilityList 
                    availability={availability}
                    onAdd={handleAdd}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                />
            )
            }

            {(addingDay || editingAvailability) && (
                <div className="form-overlay">
                    <div className="form-overlay-panel">
                        <AvailabilityForm
                            availability={
                                editingAvailability ?? 
                                undefined
                            }
                            initialDay={
                                addingDay ??
                                undefined
                            }
                            onSave={handleSave}
                            onCancel={handleCancel}
                            isSaving={isSaving}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}

