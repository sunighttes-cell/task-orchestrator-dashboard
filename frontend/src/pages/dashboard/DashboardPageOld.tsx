//Create Dashboard Page

import CreateJobForm from "@/pages/jobs/components/CreateJobForm";
import DisplayJobList from "@/pages/jobs/components/StatusGrid";
import { useJobs} from "@/hooks/useJobs";
import { useJobSummary } from "@/hooks/useJobSummary";
import SummaryGrid from "@/pages/jobs/components/SummaryGrid";
import { useSearchParams } from "react-router-dom";
import { useCallback, useEffect } from "react";
import { useJobFilterStore } from "../../store/useJobFilterStore";
import { useDebounce } from "use-debounce";
import { JobStatusFilter } from "@/pages/jobs/components/JobStatusFilter";
import type { JobStatusFilter as JobStatusFilterValue } from "@/types/job";

const statuses: JobStatusFilterValue[] = ["ALL", "QUEUED", "RUNNING", "COMPLETED", "FAILED"];

function parseStatus(value: string | null): JobStatusFilterValue {
  return statuses.includes(value as JobStatusFilterValue) ? (value as JobStatusFilterValue) : "ALL";
}

export function Dashboard() {
  const [searchParams, setSearchParams] = useSearchParams();
  const search = useJobFilterStore((s) => s.search);
  const status = useJobFilterStore((s) => s.status);
  const setSearch = useJobFilterStore((s) => s.setSearch);
  const setStatus = useJobFilterStore((s) => s.setStatus);
  const [debouncedSearch] = useDebounce(search, 500);

  const { data: jobList, error, isLoading, refetch } = useJobs({
    search: debouncedSearch,
    status,
  });

  const {data: datasummary} = useJobSummary();

  const refetchJobs = useCallback(() => {
    void refetch();
  }, [refetch]);

  //URL-driven state // SPA navigation patterns // dashboard persistence
  useEffect(() => {
    setSearch(searchParams.get("search") ?? "");
    setStatus(parseStatus(searchParams.get("status")));
  }, [searchParams, setSearch, setStatus]);

  useEffect(() => {
    const params = new URLSearchParams();
    if (debouncedSearch.trim()) {
      params.set("search", debouncedSearch.trim());
    }
    if (status !== "ALL") {
      params.set("status", status);
    }
    setSearchParams(params, { replace: true });
  }, [debouncedSearch, status, setSearchParams]);


  return (
    <div>
      <JobStatusFilter />
      <SummaryGrid datasummary={datasummary}/>
      <CreateJobForm onJobCreated={refetchJobs} />
      {isLoading && <p>Loading jobs...</p>}
      {error && <p>Failed to load jobs: {error.message}</p>}
      {jobList && <DisplayJobList jobs={jobList} isLoading={isLoading} error={error} onRetry={refetchJobs} />}
    </div>
  );
}
