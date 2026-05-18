import { useMemo } from "react";
import { SimpleDataTable } from "@/components/tables/SimpleDataTable";

export default function JobsDataTable({ jobs, totalPages, page, handlePageChange}) {
  
  const columns = useMemo(() => [
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