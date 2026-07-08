//summary hook to get job summary data
import { useQuery } from "@tanstack/react-query";
import { getStatusSummary } from "@/api/jobsApi";
import type { StatusSummaryResponse } from "@/types/job";
import { queryKeys } from "@/api/queryKeys";

export function useJobSummary() {
  const query = useQuery<StatusSummaryResponse[], Error>({
    queryKey: queryKeys.statusSummary,
    queryFn: () => getStatusSummary(),
    refetchInterval: 900000, // Polling every 10 seconds
  });

  return query;
}

