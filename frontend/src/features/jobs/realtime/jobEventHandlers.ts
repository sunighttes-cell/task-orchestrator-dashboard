// jobEventHandlers.ts

import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";
import { queryKeys } from "@/api/queryKeys";

export async function handleJobEvent(
  event: JobEvent,
  queryClient: QueryClient,
) {
  console.log("Handling SSE event:", event.eventType, "jobId:", event.jobId, "status:", event.jobStatus);
  try{
    await invalidateJobQueries(queryClient);
  } catch (error) {
    console.error("Error invalidating job queries:", error);
  }

}

export async function invalidateJobQueries(
  queryClient: QueryClient,
) {
  console.log("========== SSE REFRESH START ==========");

  const results = await Promise.all([
    queryClient.refetchQueries({
      queryKey: ["jobs"],
      type: "active",
    }),

    queryClient.refetchQueries({
      queryKey: ["status-summary"],
      type: "active",
    }),

    queryClient.refetchQueries({
      queryKey: ["dashboard-metrics"],
      type: "active",
    }),
  ]);

  console.log("========== SSE REFRESH COMPLETE ==========");

  console.log("Jobs cache:",
    queryClient.getQueriesData({
      queryKey: ["jobs"],
    })
  );

  console.log("Summary cache:",
    queryClient.getQueryData(
      queryKeys.statusSummary
    )
  );

  console.log("Metrics cache:",
    queryClient.getQueryData(
      queryKeys.dashboardMetrics
    )
  );

  console.log("Refetch results:", results);
}