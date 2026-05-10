//Create Dashboard Page

import CreateJobForm from "@/components/jobs/CreateJobForm";
import DisplayJobList from "@/components/jobs/DisplayJobList";
import { useJobs } from "@/hooks/useJobs";
import SummaryGrid from "@/components/jobs/SummaryGrid";

export function Dashboard() {
  const { data: jobList, error, isLoading, refetch } = useJobs();

  return (
    <div>
      <h2 className="text-2xl font-semibold">
        Dashboard
      </h2>
      <p className="mt-4">
        Welcome to the Task Orchestrator Dashboard! Here you can manage your tasks and monitor their status.
      </p>  
      <CreateJobForm />
      <DisplayJobList jobs={jobList} error={error} isLoading={isLoading} onRetry={() => refetch()} />
      <SummaryGrid jobs={jobList} />
    </div>
  );
}
