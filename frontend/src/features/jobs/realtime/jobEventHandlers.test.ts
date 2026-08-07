import { describe, expect, it, vi } from "vitest";
import { handleJobEvent } from "./jobEventHandlers";
import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";

describe("handleJobEvent", () => {
  it("invalidates dashboard queries for any realtime job event", () => {
    const invalidateQueries = vi.fn();
    const queryClient = { invalidateQueries } as unknown as QueryClient;
    const event: JobEvent = {
      jobId: "1",
      username: "testuser",
      eventType: "JOB_RETRIED",
      jobStatus: "FAILED",
    };

    handleJobEvent(event, queryClient);

    expect(invalidateQueries).toHaveBeenCalledTimes(3);
    expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: ["jobs"], exact: false });
    expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: ["status-summary"], exact: false });
    expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: ["dashboard-metrics"], exact: false });
  });
});
