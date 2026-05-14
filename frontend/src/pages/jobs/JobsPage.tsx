import { PageHeader } from "@/layout/PageHeader";
import JobsDataTable from "./JobsDataTable";
import { useJobs } from "@/hooks/useJobs";
import { useMemo, useState, useEffect, useCallback } from "react";
import {useSearchParams} from "react-router-dom";
import { useJobSummary } from "@/hooks/useJobSummary";
import SummaryCard from "@/pages/jobs/components/SummaryCard"
import CreateJobForm from "@/pages/jobs/components/CreateJobForm"
import {useDebounce} from "@/hooks/useDebounce";
import { Skeleton } from "@/components/ui/skeleton";
import { Empty, EmptyDescription, EmptyTitle } from "@/components/ui/empty";
//import type { JobStatusFilter as JobStatusFilterValue } from "@/types/job";

// const statuses: JobStatusFilterValue[] = ["ALL", "QUEUED", "RUNNING", "COMPLETED", "FAILED"];

// function parseStatus(value: string | null): JobStatusFilterValue {
//   return statuses.includes(value as JobStatusFilterValue) ? (value as JobStatusFilterValue) : "ALL";
// }

export default function JobsPage() {
  //data // first load // background refresh
  const { data: jobs, isLoading, isFetching } = useJobs();
  const { data: statusSummary } = useJobSummary();
  const [searchParams, setSearchParams] = useSearchParams();

  const statusFromUrl = searchParams.get("status");
  const searchFromUrl = searchParams.get("search") ?? "";

  const [search, setSearch] = useState(searchFromUrl);
  const [statusFilter, setStatusFilter] = useState<string | null>(
    statusFromUrl
  );

  const debouncedSearch = useDebounce(search, 300);

  // const filteredJobs = useMemo(() => {
  //   if (!statusFilter) return jobs;
  //   return jobs.filter((job) => job.status === statusFilter);
  // }, [jobs, statusFilter]);

  // const refetchJobs = useCallback(() => {
  //   void refetch();
  // }, [refetch]);

  // sync URL
  // useEffect(() => {
  //   if (statusFilter) {
  //     setSearchParams({ status: statusFilter });
  //   } else {
  //     setSearchParams({});
  //   }
  // }, [statusFilter]);

  const filteredJobs = useMemo(() => {
      return jobs.filter((job) => {

    const matchesStatus = statusFilter ? job.status === statusFilter : true;
    const matchesSearch = debouncedSearch
      ? job.name.toLowerCase().includes(debouncedSearch.toLowerCase()) : true;
      return matchesStatus && matchesSearch;
    });
  }, [jobs, statusFilter, debouncedSearch]);

  useEffect(() => {
    const params: Record<string, string> = {};

    if (statusFilter) params.status = statusFilter;
    if (debouncedSearch) params.search = debouncedSearch;
    setSearchParams(params);
  }, [statusFilter, debouncedSearch]);

  return (
      <div className="space-y-6 p-6">
        <PageHeader title="Jobs" description="Browse and manage orchestration jobs" />
        <div><CreateJobForm /></div>
        <div className="flex gap-4 mb-4">
          {statusSummary.map((item) => (
            <SummaryCard
              key={item.status}
              summary={item}
              isSelected={statusFilter === item.status}
              onClick={() =>
                setStatusFilter((prev) => prev === item.status ? null : item.status)
              }
            />
          ))}
        </div>
        {/* Search */}
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search jobs..."
          className="mb-4 p-2 border rounded w-full"
        />
        {isFetching && (
          <div className="text-sm text-gray-500 mb-2">
            <Empty>Updating...</Empty>
          </div>
        )}
        {isLoading ? (
            <Skeleton />
        ) : !filteredJobs || filteredJobs.length === 0 ? (
            <Empty>
              <EmptyTitle>No Jobs Found</EmptyTitle>
              <EmptyDescription>
                Try adjusting your search or filter to find what you're looking for.
              </EmptyDescription>
            </Empty>
        ) : (
            <JobsDataTable jobs={filteredJobs ?? []} />
        )}
      </div>
  );
}
