
export const queryKeys = {
  jobs: (filters: Record<string, any>) =>
    ["jobs", JSON.stringify(filters ?? {})],

  job: (id: number) => ["job", id],

  profile: ["profile"],
  
  login: (loginData: Record<string, string>) => 
    ["login", JSON.stringify(loginData)],

  statusSummary: ["status-summary"],
  dashboardMetrics: ["dashboard-metrics"],
};