//Job List Component
import StatusCard from "@/components/cards/StatusCard";
import type { Job } from "@/types/job";
import {Skeleton} from "@/components/ui/skeleton"
import { EmptyData } from "@/components/EmptyData";

interface Props {
  jobs: Job[] | undefined;
  isLoading: boolean;
  onRetry: (jobId: number) => void;
}

const DisplayJobList: React.FC<Props> = ({ jobs, isLoading, onRetry }) => {
  return (
  <>
  {isLoading ? ( <div role="status" aria-label="Loading"><Skeleton /><span className="sr-only">Loading...</span></div> ) : !jobs|| jobs.length === 0 ? (<EmptyData />) :
    (
      <div>
        <h2 className="text-lg font-semibold text-center mb-4">Job List</h2>
        <ul className="grid grid-cols-1 md:grid-cols-1 lg:grid-cols-1 gap-4">
          {jobs.map((job) => (
            <li key={job.id}>
              <StatusCard job={job} onRetry={onRetry} />
            </li>
          ))}
        </ul>
      </div>
    )}
    </>
  );
};

export default DisplayJobList;
