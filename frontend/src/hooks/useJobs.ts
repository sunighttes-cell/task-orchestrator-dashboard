import { useQuery, keepPreviousData} from "@tanstack/react-query";
import { getJobs } from "@/api/jobsApi";

export function useJobs(filters: {}) {
  return useQuery({
    queryKey: ["jobs", filters], // cache per filter set
    queryFn: () => getJobs(filters),
    placeholderData: keepPreviousData, // smooth pagination
    refetchInterval: 5000,
  });
}