import { useQuery } from "@tanstack/react-query";
import { getMetrics } from "@/api/dashboardApi";

export function useDashboardMetrics() {
  return useQuery({
    queryKey: ["dashboard-metrics"],
    queryFn: getMetrics,
    refetchInterval: 10000,
  })
}
