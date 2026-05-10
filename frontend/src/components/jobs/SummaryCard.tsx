//Summary Card Component
import type { Job } from "@/types/job";

interface Props {
  job: Job;
}

const SummaryCard: React.FC<Props> = ({ job }) => {
  return (
    <div className="border p-4 rounded">
      <h3>{job.name}</h3>
      <p>{job.description}</p>
      <p>Status: {job.status}</p>
    </div>
  );
};

export default SummaryCard;     

