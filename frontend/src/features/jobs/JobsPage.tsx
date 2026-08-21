import { PageHeader } from "@/layout/PageHeader";
import JobsDataTable from "./JobsDataTable";
import {FilterDropdown} from "@/components/FilterDropdown";
import { useJobs } from "@/hooks/jobs/useJobs";
import {useState} from "react";
import CreateJobForm from "@/features/jobs/components/CreateJobForm";
import { Skeleton } from "@/components/ui/skeleton";
import { Empty } from "@/components/ui/empty";
import { FilterSearch } from "@/components/FilterSearch";
import {JobStatusFilterValues} from "@/lib/constants"
import { useJobFilters } from "@/hooks/jobs/useJobFilters";
import { EmptyData } from "@/components/EmptyData";
import type { JobStatus, Job } from "@/types/job";

export default function JobsPage() {
  //data // first load // background refresh
  //get data
  const { filters, searchParams, setSearchParams } = useJobFilters();
  const { data: jobsData, isLoading, isFetching, isError } = useJobs(filters);
  //handle search
  const [searchInput, setSearchInput] = useState(filters.search ?? "");

  const jobs = jobsData?.content ?? [];
  const totalPages = jobsData?.totalPages ?? 0;
  const currentPage = jobsData?.number ?? filters.page;

  //handle page change
  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", String(newPage));
    setSearchParams(params);
  };

  const handleSearchChange = (value: string) => {
    setSearchInput(value);
    const params = new URLSearchParams(searchParams);

    if (value.trim()) {
      params.set("search", value.trim());
    } else {
      params.delete("search");
    }

    params.set("page", "0");

    setSearchParams(params);
  };

  //handle status/filter change
  const handleStatusChange = (value: string) => {
    const params = new URLSearchParams(searchParams);

    if (!value || value.toLowerCase() === "all") {
      params.delete("status");
    } else {
      params.set("status", value);
    }

    params.set("page", "0");

    setSearchParams(params);
  };

  return (
      <div className="space-y-6 p-6">
        <PageHeader title="Jobs" description="Browse and manage orchestration jobs"/>
        {/* Create Jobs */}
        <div><CreateJobForm/></div>
        {/* Search Jobs*/}
        <FilterSearch search={searchInput} onChange={handleSearchChange}/>
        {/*Filter Jobs*/}
        <FilterDropdown 
        status={filters.status as JobStatus ?? "ALL" as JobStatus}
        optionValues={JobStatusFilterValues as string[]} 
        onChange = {handleStatusChange}>
        </FilterDropdown>
        {isFetching && (
          <div className="text-sm text-gray-500 mb-2">
            <Empty>Updating...</Empty>
          </div>
        )}
        {isLoading ? (
            <div role="status" aria-label="Loading">
              <Skeleton />
              <span className="sr-only">Loading...</span>
            </div>
        ) : isError ? (
            <div role="alert">Error loading jobs</div>
        ) : !jobs  || jobs.length === 0 ? (<EmptyData/>) : (
            <JobsDataTable 
            jobs={jobs} 
            totalPages={totalPages} 
            handlePageChange={handlePageChange}
            page={currentPage}/>
        )}
      </div>
  );
}
