import type { DragEvent } from "react";

interface TaskDropZoneProps {
    onDragOver: (event: DragEvent<HTMLDivElement>) => void;
    onDrop: (event: DragEvent<HTMLDivElement>) => void;
    active: boolean;
}

export default function TaskDropZone({
    onDragOver,
    onDrop,
    active,
}: TaskDropZoneProps) {
    return (
        <div
            className={`task-drop-zone ${active
                ? "active"
                : ""
            }`}
            onDragOver={onDragOver}
            onDrop={onDrop}
        />
    );
}