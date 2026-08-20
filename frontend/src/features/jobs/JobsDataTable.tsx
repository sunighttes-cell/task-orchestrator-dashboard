import { SimpleDataTable } from "@/components/tables/SimpleDataTable";
import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import type { Job } from "@/types/job";

interface JobsDataTableProps {
  jobs: Job[];
  totalPages: number;
  page: number;
  handlePageChange: (page: number) => void;
}

export default function JobsDataTable({ jobs, totalPages, page, handlePageChange}: JobsDataTableProps) {
  
  const columns = useMemo<ColumnDef<Job, unknown>[]>(() => [
    {
      header: "Job ID",
      accessorKey: "id",
    },
    {
      header: "Name",
      accessorKey: "name",
    },
    {
      header: "Status",
      accessorKey: "status",
    },
  ], []);

  return <SimpleDataTable 
  columns={columns} data={jobs} 
  totalPages={totalPages} 
  page={page} 
  handlePageChange={handlePageChange}/>;
}