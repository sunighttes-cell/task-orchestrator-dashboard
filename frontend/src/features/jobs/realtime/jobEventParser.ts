import type { JobEvent } from "@/types/jobEvents";

export default function parseSseEvent(rawEvent: string): JobEvent | null {
    const lines = rawEvent.split(/\r?\n/);

    let eventType: string | undefined;
    const dataLines: string[] = [];

    for (const line of lines) {
        if (line.startsWith("event:")) {
        eventType = line.slice("event:".length).trim();
        }

        if (line.startsWith("data:")) {
        dataLines.push(line.slice("data:".length).trim());
        }
    }

    if (dataLines.length === 0) {
        return null;
    }

    const data = JSON.parse(dataLines.join("\n"));

    return {
        ...data,
        type: eventType ?? data.type,
    };
}