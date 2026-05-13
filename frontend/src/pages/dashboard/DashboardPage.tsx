import { PageHeader } from "@/layout/PageHeader";
import MetricsGrid from "@/components/dashboard/MetricsGrid";
import SummaryGrid from "../jobs/components/SummaryGrid";
import { useDashboardMetrics } from "@/hooks/useDashboardMetrics";
import { useJobSummary } from "@/hooks/useJobSummary";

export default function DashboardPage() {
  const { data: metrics } = useDashboardMetrics();
  const { data: statusSummary } = useJobSummary();

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Monitor orchestration jobs" />
      <MetricsGrid metrics={metrics} />
      <SummaryGrid datasummary={statusSummary} />
    </div>
  );
}
