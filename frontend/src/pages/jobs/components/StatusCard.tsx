//job card component
import { retryJob } from "@/api/jobsApi";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import type { Job } from "@/types/job";
import {Button} from "@/components/ui/button";
import JobStatusBadge from "@/pages/jobs/components/JobStatusBadge";

interface Props {
  job: Job;
  onRetry: (jobId: number) => void;
}

const StatusCard: React.FC<Props> = ({ job, onRetry }) => {
  const handleRetry = async () => {
    try {
      await retryJob(job.id);
      onRetry(job.id); // Notify parent to refresh job list
    } catch (error) {
      console.error("Failed to retry job:", error);
    }
  };

  return (
    <Card className="border p-4 rounded dark:bg-gray-900">
      <CardHeader>
        <h3 className="text-lg font-bold">{job.name}</h3>
        <p>Status: </p> 
        <JobStatusBadge status={job.status} />
      </CardHeader>
      <CardContent>
        <p>Created At: {new Date(job.createdAt).toLocaleString()}</p>
        <p>Retry Count: {job.retryCount}</p>
        <Button onClick={handleRetry} disabled={job.status !== "FAILED"}>
          Retry
        </Button>
      </CardContent>
    </Card>
  );
}

export default StatusCard;