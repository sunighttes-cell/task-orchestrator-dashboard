//Create Dashboard Page

import CreateJobForm from "@/components/jobs/CreateJobForm";
import DisplayJobList from "@/components/jobs/DisplayJobList";
import { useJobs } from "@/hooks/useJobs";

export function Dashboard() {
  const { data: jobList, error, isLoading } = useJobs();

  return (
    <div>
      <h2 className="text-2xl font-semibold">
        Dashboard
      </h2>
      <p className="mt-4">
        Welcome to the Task Orchestrator Dashboard! Here you can manage your tasks and monitor their status.
      </p>  
      <CreateJobForm />
      <DisplayJobList jobs={jobList} error={error} isLoading={isLoading} onRetry={() => {}} />
    </div>
  );
}