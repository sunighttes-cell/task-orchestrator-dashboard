//Job List Component
import StatusCard from "@/pages/jobs/components/StatusCard";
import type { Job } from "@/types/job";

interface Props {
  jobs: Job[] | undefined;
  isLoading: boolean;
  error: Error | null;
  onRetry: (jobId: number) => void;
}

const DisplayJobList: React.FC<Props> = ({ jobs, isLoading, error, onRetry }) => {
  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading jobs: {error.message}</div>;
  if (!jobs || jobs.length === 0) return <div>No jobs found</div>;

  return (
    <div>
      <h2 className="text-lg font-semibold">Job List</h2>
      <ul className="text-sm text-muted-foreground">
        {jobs.map((job) => (
          <li key={job.id}>
            <StatusCard job={job} onRetry={onRetry} />
          </li>
        ))}
      </ul>
    </div>
  );
};

export default DisplayJobList;
