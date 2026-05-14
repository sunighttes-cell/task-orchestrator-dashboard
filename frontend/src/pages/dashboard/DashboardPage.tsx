import { PageHeader } from "@/layout/PageHeader";
import MetricsGrid from "@/components/dashboard/MetricsGrid";
import SummaryGrid from "../jobs/components/SummaryGrid";
import { useDashboardMetrics } from "@/hooks/useDashboardMetrics";
import { useJobSummary } from "@/hooks/useJobSummary";
import { useJobs } from "@/hooks/useJobs";
import StatusGrid from "@/pages/jobs/components/StatusGrid";

export default function DashboardPage() {
  const { data: metrics } = useDashboardMetrics();
  const { data: statusSummary } = useJobSummary();
  const { data: jobs = [], isLoading, error } = useJobs();

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Monitor orchestration jobs" />
      <MetricsGrid metrics={metrics} />
      <SummaryGrid datasummary={statusSummary} />
      <StatusGrid jobs={jobs} isLoading={isLoading} error={error} onRetry={() => {}}/>
    </div>
  );
}