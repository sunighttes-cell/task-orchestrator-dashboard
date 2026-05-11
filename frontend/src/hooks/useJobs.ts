//uses getJobs to fetch jobs, handles cache invalidation, refetching, polling, retries, async state management. used in the JobList for display.

import { useQuery } from "@tanstack/react-query";
import { getJobs } from "@/api/jobsApi";
import type { Job, JobsQueryFilters } from "@/types/job";

export function useJobs(filters: JobsQueryFilters = {}) {
  //useQuery to fetch jobs; polling every5secs. Query key:["jobs", filters]. filters for caching and invalidation
  const query = useQuery<Job[], Error>({
    queryKey: ["jobs", filters],
    queryFn: () => getJobs(filters),
    refetchInterval: 5000, // Polling interval
  });

  // query object including data, error, & loading state
  return query;
}
