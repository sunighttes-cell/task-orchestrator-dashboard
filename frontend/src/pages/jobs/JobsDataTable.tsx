import { useMemo } from "react";
import { SimpleDataTable } from "@/components/tables/SimpleDataTable";

export default function JobsDataTable({ jobs }) {
  
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

  return <SimpleDataTable columns={columns} data={jobs} />;
}