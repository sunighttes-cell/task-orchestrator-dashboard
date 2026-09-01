import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";

export async function handleJobEvent(
  event: JobEvent,
  queryClient: QueryClient,
) {
  console.log(
    "Handling SSE event:",
    event.eventType,
    "jobId:",
    event.jobId,
    "status:",
    event.jobStatus
  );

  try {
    await invalidateJobQueries(queryClient);
  } catch (error) {
    console.error("Error invalidating job queries:", error);
  }
}

export async function invalidateJobQueries(
  queryClient: QueryClient,
) {
  console.log("========== SSE REFRESH START ==========");

  await Promise.all([
    queryClient.invalidateQueries({
      queryKey: ["jobs"],
      refetchType: "all",
    }),

    queryClient.invalidateQueries({
      queryKey: ["status-summary"],
      refetchType: "all",
    }),

    queryClient.invalidateQueries({
      queryKey: ["dashboard-metrics"],
      refetchType: "all",
    }),
  ]);

  console.log("========== SSE REFRESH COMPLETE ==========");
  console.log("Jobs cache:", queryClient.getQueriesData({ queryKey: ["jobs"]}));
  console.log("Summary cache:", queryClient.getQueryData(["status-summary"]));
  console.log("Metrics cache:", queryClient.getQueryData(["dashboard-metrics"]));
  console.log("========== SSE REFRESH END ==========");
}