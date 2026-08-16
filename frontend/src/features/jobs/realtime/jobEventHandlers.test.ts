import { describe, expect, it, vi } from "vitest";
import { handleJobEvent } from "./jobEventHandlers";
import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";

describe("handleJobEvent", () => {
  it("invalidates jobs, summary, and metrics for a realtime job event", async () => {
    const invalidateQueries = vi.fn().mockResolvedValue(undefined);

    const queryClient = {
      invalidateQueries,
    } as unknown as QueryClient;

    const event: JobEvent = {
      jobId: "1",
      username: "testuser",
      eventType: "JOB_RETRIED",
      jobStatus: "FAILED",
    };

    await handleJobEvent(event, queryClient);

    expect(invalidateQueries).toHaveBeenCalledTimes(3);

    expect(invalidateQueries).toHaveBeenCalledWith({
      queryKey: ["jobs"],
      refetchType: "all",
    });

    expect(invalidateQueries).toHaveBeenCalledWith({
      queryKey: ["status-summary"],
      refetchType: "all",
    });

    expect(invalidateQueries).toHaveBeenCalledWith({
      queryKey: ["dashboard-metrics"],
      refetchType: "all",
    });
  });
});