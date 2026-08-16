import { useQuery } from "@tanstack/react-query";
import { getMetrics } from "@/api/dashboardApi";
import { queryKeys } from "@/api/queryKeys";

export function useDashboardMetrics() {
  return useQuery({
    queryKey: queryKeys.dashboardMetrics,
    queryFn: getMetrics,
  });
}