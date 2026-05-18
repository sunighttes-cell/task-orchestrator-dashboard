import { apiClient } from "@/api/client";
import type { DashboardMetrics } from "@/types/job";

export async function getMetrics(): Promise<DashboardMetrics> {
  const response = await apiClient.get<DashboardMetrics>("/jobs/dashboard-metrics")
  return response.data
}
