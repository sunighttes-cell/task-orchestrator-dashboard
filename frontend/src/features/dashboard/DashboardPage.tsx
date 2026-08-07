import { PageHeader } from "@/layout/PageHeader";
import MetricsGrid from "@/components/cards/MetricsGrid";
import SummaryGrid from "@/components/cards/SummaryGrid";
import { useDashboardMetrics } from "@/hooks/dashboard/useDashboardMetrics";
import { useJobSummary } from "@/hooks/jobs/useJobSummary";
import { useJobs } from "@/hooks/jobs/useJobs";
import StatusGrid from "@/components/cards/StatusGrid";
import {useJobFilters} from "@/hooks/jobs/useJobFilters"
import { useEffect } from "react";

export default function DashboardPage() {
  const { data: metrics, isLoading: isLoadingMetrics} = useDashboardMetrics();
  const { data: statusSummary, isLoading: isLoadingSummary} = useJobSummary();
  const { filters, searchParams, setSearchParams } = useJobFilters();
  const { data: jobsData, isLoading: isLoadingJobs } = useJobs(filters);
  const jobs = jobsData?.content ?? [];
  const navUrl = `/jobs?status=`;

  console.log("Dashboard data:", {metrics, statusSummary, jobs});
  useEffect(() => {
    console.log("Dashboard jobs changed", jobs);
  }, [jobs]);

  useEffect(() => {
      console.log("Metrics changed", metrics);
  }, [metrics]);

  useEffect(() => {
      console.log("Summary changed", statusSummary);
  }, [statusSummary]);

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Monitor orchestration jobs" />
      <MetricsGrid metrics={metrics} isLoading={isLoadingMetrics}/>
      <SummaryGrid datasummary={statusSummary} isLoading ={isLoadingSummary}
      navigateBaseURL={navUrl}/>
      <StatusGrid jobs={jobs} isLoading={isLoadingJobs} onRetry={() => {}}/>
    </div>
  );
}