//summary hook to get job summary data
import { useQuery } from "@tanstack/react-query";
import { getStatusSummary } from "@/api/jobsApi";
import type { StatusSummaryResponse } from "@/types/job";

export function useJobSummary() {
  const query = useQuery<StatusSummaryResponse[], Error>({
    queryKey: ["status-summary"],
    queryFn: () => getStatusSummary(),
    refetchInterval: 10000, // Polling every 10 seconds
  });

  return query;
}

