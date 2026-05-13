import { apiClient } from "@/api/client";
import type { DashboardMetrics } from "@/types/job";

//const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function getMetrics(): Promise<DashboardMetrics> {
  const response = await apiClient.get<DashboardMetrics>("/jobs/dashboard-metrics")
  return response.data
}
