//Summary Grid Component
import SummaryCard from "@/pages/jobs/components/SummaryCard";
import type { StatusSummaryResponse } from "@/types/job";

//queued //running //completed //failed
interface Props {
  datasummary: StatusSummaryResponse[] | undefined;
}

const SummaryGrid: React.FC<Props> = ({ datasummary }) => {
  if (!datasummary || datasummary.length === 0) return <div>No jobs found</div>;
  return (
    <div>
      <h2 className="text-lg font-semibold">Job Summary</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {datasummary.map((summary) => (
          <SummaryCard key={summary.status} summary={summary} />
        ))}
      </div>
    </div>
  );
};

export default SummaryGrid;     


