import type { JobFilters } from "@/hooks/jobs/useJobs";

export const queryKeys = {
  jobs: (filters: JobFilters) =>
    ["jobs", JSON.stringify(filters ?? {})],

  job: (id: number) => ["job", id],

  profile: ["profile"],
  
  login: (loginData: Record<string, string>) => 
    ["login", JSON.stringify(loginData)],

  statusSummary: ["status-summary"],
  dashboardMetrics: ["dashboard-metrics"],
};