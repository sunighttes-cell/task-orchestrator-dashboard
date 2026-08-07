// jobEventHandlers.ts

import type { QueryClient } from "@tanstack/react-query";
import type { JobEvent } from "@/types/jobEvents";
import { queryKeys } from "@/api/queryKeys";
import { getMetrics } from "@/api/dashboardApi";
import { getStatusSummary } from "@/api/jobsApi";
import { getJobs } from "@/api/jobsApi";

export function handleJobEvent(
  event: JobEvent,
  queryClient: QueryClient,
) {
  switch (event.eventType) {
    case "JOB_CREATED":
      invalidateJobQueries(queryClient);
      break;
    case "JOB_STARTED":
      invalidateJobQueries(queryClient);
      break;
    case "JOB_COMPLETED":
      invalidateJobQueries(queryClient);
      break;
    case "JOB_FAILED":
      invalidateJobQueries(queryClient);
      break;
    case "JOB_RETRIED":
      invalidateJobQueries(queryClient);
      break;
    default:
      invalidateJobQueries(queryClient);
  }
}

export async function invalidateJobQueries(
  queryClient: QueryClient,
) {
  console.log("Refetching queries...");
  console.log("isFetching:", queryClient.isFetching());
  await Promise.all([
    queryClient.fetchQuery({
      queryKey: queryKeys.jobs({}),
      queryFn: () => getJobs({}),
    }),
    queryClient.fetchQuery({
      queryKey: queryKeys.statusSummary,
      queryFn: getStatusSummary,
    }),
    queryClient.fetchQuery({
      queryKey: queryKeys.dashboardMetrics,
      queryFn: getMetrics,
    }),
]);
}