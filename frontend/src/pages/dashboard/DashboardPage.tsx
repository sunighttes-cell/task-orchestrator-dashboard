import { PageHeader } from "@/layout/PageHeader";
import MetricsGrid from "@/components/cards/MetricsGrid";
import SummaryGrid from "@/components/cards/SummaryGrid";
import { useDashboardMetrics } from "@/hooks/useDashboardMetrics";
import { useJobSummary } from "@/hooks/useJobSummary";
import { useJobs } from "@/hooks/useJobs";
import StatusGrid from "@/components/cards/StatusGrid";
import {useJobFilters} from "@/hooks/useJobFilters"

export default function DashboardPage() {
  const { data: metrics, isLoading: isLoadingMetrics} = useDashboardMetrics();
  const { data: statusSummary, isLoading: isLoadingSummary} = useJobSummary();
  const { filters, searchParams, setSearchParams } = useJobFilters();
  const { data, isLoading: isLoadingJobs } = useJobs(filters);
  const jobs = data?.content ?? [];
  const navUrl = `/jobs?status=`;

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