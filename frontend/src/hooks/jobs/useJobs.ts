import { useQuery, keepPreviousData} from "@tanstack/react-query";
import { getJobs } from "@/api/jobsApi";
import { queryKeys } from "@/api/queryKeys";

export function useJobs(filters: {}) {
  return useQuery({
    queryKey: queryKeys.jobs(filters),
    queryFn: () => getJobs(filters),
    placeholderData: keepPreviousData, // smooth pagination
    // refetchInterval: 5000,
    //refetchInterval: 900000,
    //staleTime: 5 * 900000,
  });
}