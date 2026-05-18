import { PageHeader } from "@/layout/PageHeader";
import JobsDataTable from "./JobsDataTable";
import {FilterDropdown} from "@/components/FilterDropdown";
import { useJobs } from "@/hooks/useJobs";
import {useState, useEffect} from "react";
import CreateJobForm from "@/pages/jobs/components/CreateJobForm";
import {useDebounce} from "@/hooks/useDebounce";
import { Skeleton } from "@/components/ui/skeleton";
import { Empty, EmptyDescription, EmptyTitle } from "@/components/ui/empty";
import { FilterSearch } from "@/components/FilterSearch";
import {JobStatusFilterValues} from "@/lib/constants"
import { useJobFilters } from "@/hooks/useJobFilters";
import { EmptyData } from "@/components/EmptyData";

export default function JobsPage() {
  //data // first load // background refresh

  //get data
  const { filters, searchParams, setSearchParams } = useJobFilters();
  const { data, isLoading, isFetching, isError } = useJobs(filters);
  const jobs = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  //handle page change
  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams);
    params.set("page", String(newPage));
    setSearchParams(params);
  };

  //handle search
  const [searchInput, setSearchInput] = useState(filters.search ?? "");
  const debouncedSearch = useDebounce(searchInput, 300);

  useEffect(() => {
    const params = new URLSearchParams(searchParams);

    if (debouncedSearch) {
      params.set("search", debouncedSearch);
    } else {
      params.delete("search");
    }

    params.set("page", "0");

    setSearchParams(params);
  }, [debouncedSearch]);

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

  useEffect(() => {
    setSearchInput(filters.search ?? "");
  }, [filters.search]);

  return (
      <div className="space-y-6 p-6">
        <PageHeader title="Jobs" description="Browse and manage orchestration jobs" />
        {/* Create Jobs */}
        <div><CreateJobForm/></div>
        {/* Search Jobs*/}
        <FilterSearch search={searchInput} onChange={setSearchInput}/>
        {/*Filter Jobs*/}
        <FilterDropdown 
        status={filters.status ?? "ALL"}
        optionValues={JobStatusFilterValues} 
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
        ) : !jobs  || jobs.length === 0 ? (<EmptyData/>
) : (
            <JobsDataTable 
            jobs={jobs} 
            totalPages={totalPages} 
            handlePageChange={handlePageChange}
            page={filters.page}/>
        )}
      </div>
  );
}
