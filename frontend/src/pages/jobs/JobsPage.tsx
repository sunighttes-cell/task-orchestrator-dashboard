import { DataTableProvider } from "@/providers/DataTableProvider";
import { PageHeader } from "@/layout/PageHeader";
import JobsDataTable from "./JobsDataTable";
import { useJobs } from "@/hooks/useJobs";

export default function JobsPage() {
  const { data: jobs } = useJobs();

  return (
    <DataTableProvider>
      <div className="space-y-6 p-6">
        <PageHeader title="Jobs" description="Browse and manage orchestration jobs" />
        <JobsDataTable jobs={jobs ?? []} />
      </div>
    </DataTableProvider>
  );
}
