//job card component
import { retryJob } from "@/api/jobsApi";
import { Card, CardAction, CardTitle } from "@/components/ui/card";
import type { Job } from "@/types/job";
import {PrimaryBtnClass, SecondaryBtnClass, StatusBgColor} from "@/lib/constants";
import {normalizeName, normalizeStatus} from "@/lib/normalizeJob";
import { Badge } from "@/components/ui/badge"
import { Label } from "@/components/ui/label"
import { useDeleteJob } from "@/hooks/useDeleteJob";
import { toast } from "sonner";

interface Props {
  job: Job;
  onRetry: (jobId: number) => void;
}

const StatusCard: React.FC<Props> = ({ job, onRetry }) => {
  const status = normalizeStatus(job.status);
  const badgeColor = StatusBgColor[status];
  const { mutate: deleteJob } = useDeleteJob();

  const handleDelete = async () => {
    try{
      if (confirm("Delete this job?")) {
        await deleteJob(job.id);
      }
    } catch(error){
      toast.error("Failed to delete job:", error)
    }
  };

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
      <CardTitle className="text-lg font-bold">{normalizeName(job.name)}</CardTitle>
        <Label >Status: 
          <Badge className={badgeColor}>{job.status}</Badge>
        </Label>
      <div className="text-md font-semi-bold">
        <p>Created At: {new Date(job.createdAt).toLocaleString()}</p>
        <p>Retry Count: {job.retryCount}</p>
      </div>
      <CardAction>
          <div className="flex gap-2 mt-4">
            <button type="button" className={SecondaryBtnClass} onClick={handleDelete}>
            Delete
            </button>
            <button type="button" className={PrimaryBtnClass} onClick={handleRetry} 
            disabled={job.status !== "FAILED"}>
            Retry
            </button>
          </div>
      </CardAction>
    </Card>
  );
}

export default StatusCard;