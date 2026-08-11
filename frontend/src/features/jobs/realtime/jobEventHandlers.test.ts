import { describe, expect, it, vi } from "vitest";
import { handleJobEvent } from "./jobEventHandlers";
import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";

describe("handleJobEvent", () => {
  it("refetches jobs, summary, and metrics for a realtime job event", async () => {
    const refetchQueries = vi.fn().mockResolvedValue(undefined);

    const queryClient = {
      refetchQueries,
    } as unknown as QueryClient;

    const event: JobEvent = {
      jobId: "1",
      username: "testuser",
      eventType: "JOB_RETRIED",
      jobStatus: "FAILED",
    };

    await handleJobEvent(event, queryClient);

    expect(refetchQueries).toHaveBeenCalledTimes(3);

    expect(refetchQueries).toHaveBeenCalledWith({
      queryKey: ["jobs"],
      type: "active",
    });

    expect(refetchQueries).toHaveBeenCalledWith({
      queryKey: ["status-summary"],
      type: "active",
    });

    expect(refetchQueries).toHaveBeenCalledWith({
      queryKey: ["dashboard-metrics"],
      type: "active",
    });
  });
});