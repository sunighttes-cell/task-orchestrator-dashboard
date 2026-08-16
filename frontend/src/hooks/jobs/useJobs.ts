import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getJobs } from "@/api/jobsApi";
import { queryKeys } from "@/api/queryKeys";

export interface JobFilters {
  status?: string;
  search?: string;
  page: number;
  size: number;
  sort: string;
}

export function useJobs(filters: JobFilters) {
  return useQuery({
    queryKey: queryKeys.jobs(filters),
    queryFn: () => getJobs(filters),
    placeholderData: keepPreviousData,
  });
}