//metrics grid
import type { DashboardMetrics } from "@/types/job";
import { MetricCard } from "./MetricCard";
import {Skeleton} from "@/components/ui/skeleton"
import { EmptyData } from "../EmptyData";

interface Props {
  metrics: DashboardMetrics | undefined;
  isLoading: boolean;
}

const MetricsGrid: React.FC<Props> = ({ metrics, isLoading}) => {
  const successRate = metrics?.successRate ?? '0';
  const displayRate = (typeof successRate === 'number') ? successRate.toFixed(2) : '0.00'; 

  return (<>
  {isLoading ? ( <div role="status" aria-label="Loading"><Skeleton /><span className="sr-only">Loading...</span></div> ) :!metrics || metrics.totalJobs === 0 ? (<EmptyData />) :
    (
      <div>
        <h2 className="text-lg font-semibold text-center mb-4">Metrics</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <MetricCard title="Total Jobs" value={metrics.totalJobs} description="Total number of jobs processed" />
          <MetricCard title="Completed Jobs" value={metrics.completedJobs} description="Number of jobs completed successfully" />
          <MetricCard title="Failed Jobs" value={metrics.failedJobs} description="Number of jobs that failed" />
          <MetricCard title="Success Rate" value={displayRate + "%"} description="Percentage of jobs completed successfully" />
          <MetricCard title="Active Workers" value={metrics.activeWorkers} description="Number of workers currently active" />
          <MetricCard title="Avg Execution Time" value={`${metrics.avgExecutionTime.toFixed(2)} sec`} description="Average time taken to execute a job" />
        </div>
      </div>
    )}
  </>
  );
};

export default MetricsGrid;
