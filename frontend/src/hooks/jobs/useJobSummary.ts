//summary hook to get job summary data
import { useQuery } from "@tanstack/react-query";
import { getStatusSummary } from "@/api/jobsApi";
import { queryKeys } from "@/api/queryKeys";

export function useJobSummary() {
  return useQuery({
    queryKey: queryKeys.statusSummary,
    queryFn: getStatusSummary,
  });
}
