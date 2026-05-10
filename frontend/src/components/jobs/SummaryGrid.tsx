//Summary Grid Component
import SummaryCard from "@/components/jobs/SummaryCard";
import type { Job } from "@/types/job";

//queued //running //completed //failed


interface Props {
  jobs: Job[] | undefined;
}

const SummaryGrid: React.FC<Props> = ({ jobs }) => {
  if (!jobs || jobs.length === 0) return <div>No jobs found</div>;

  return (
    <div>
      <h2>Job Summary</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {jobs.map((job) => (
          <SummaryCard key={job.id} job={job} />
        ))}
      </div>
    </div>
  );
};

export default SummaryGrid;     


