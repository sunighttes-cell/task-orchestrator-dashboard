//metrics grid
import type { DashboardMetrics } from "@/types/job";
import { MetricCard } from "./MetricCard";

interface Props {
  metrics: DashboardMetrics | undefined;
}

const MetricsGrid: React.FC<Props> = ({ metrics }) => {
  if (!metrics) return <div>Loading metrics...</div>;

  return (
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <MetricCard title="Total Jobs" value={metrics.totalJobs} description="Total number of jobs processed" />
    <MetricCard title="Completed Jobs" value={metrics.completedJobs} description="Number of jobs completed successfully" />
    <MetricCard title="Failed Jobs" value={metrics.failedJobs} description="Number of jobs that failed" />
    <MetricCard title="Success Rate" value={`${metrics.successRate}%`} description="Percentage of jobs completed successfully" />
    <MetricCard title="Active Workers" value={metrics.activeWorkers} description="Number of workers currently active" />
    <MetricCard title="Avg Execution Time" value={`${metrics.avgExecutionTime} sec`} description="Average time taken to execute a job" />
  </div>);
};

export default MetricsGrid;
