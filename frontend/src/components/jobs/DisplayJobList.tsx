//Job List Component
import JobCard from  "@/components/jobs/DisplayJobCard";
import type { Job } from "@/types/job";

  interface Props {
    jobs: Job[] | undefined;
    isLoading: boolean;
    error: Error | null;
    onRetry: (jobId: number) => void;
  }

const DisplayJobList: React.FC<Props> = ({ jobs, isLoading, error,        onRetry }) => {
  if (jobs === undefined || jobs === null) return <div>No jobs found</div>;
  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error loading jobs: {error.message}</div>;

  return (
    <div>
      <h2>Job List</h2>
      <ul>
        {jobs?.map((job) => (
          <li key={job.id}>
            <h3>{job.name}</h3>
            <p>{job.description}</p>
            <JobCard job={job} onRetry={onRetry} />
          </li>
        ))}
      </ul>
    </div>
  );
}

export default DisplayJobList;