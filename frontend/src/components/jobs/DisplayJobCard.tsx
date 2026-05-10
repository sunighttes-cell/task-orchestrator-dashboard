//job card component
import { retryJob } from "@/api/jobsApi";
import type { Job } from "@/types/job";
import Jobstatusbadge from "@/components/jobs/DisplayJobStatusBadge";

interface Props {
  job: Job;
  onRetry: (jobId: number) => void;
}

const DisplayJobCard: React.FC<Props> = ({ job, onRetry }) => {
  const handleRetry = async () => {
    try {
      await retryJob(job.id);
      onRetry(job.id); // Notify parent to refresh job list
    } catch (error) {
      console.error("Failed to retry job:", error);
    }
  };

  return (
    <div className="border p-4 mb-2">
      <h3 className="text-lg font-bold">{job.name}</h3>
      <p>Status: </p> 
      <Jobstatusbadge status={job.status} />
      <p>Created At: {new Date(job.createdAt).toLocaleString()}</p>
      <p>Retry Count: {job.retryCount}</p>
      <button onClick={handleRetry} disabled={job.status !== "FAILED"}>
        Retry
      </button>
    </div>
  );
}

export default DisplayJobCard;