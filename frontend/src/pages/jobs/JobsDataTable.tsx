import {useEffect, useMemo, useState} from "react";
import type {ColumnDef, Table} from "@tanstack/react-table";
import {isWithinInterval} from "date-fns";
import {AdvancedDataTable} from "@/components/tables/AdvancedDataTable";
import {DataTableCheckBox} from "@/components/tables/DataTableCheckBox";
import type { Job } from "@/types/job";

interface Props {
  jobs: Job[];
}

//const data = makeData(100_000);
export default function JobsDataTable({ jobs }: Props) {
    const [isLoading, setLoading] = useState(true);
    const columns = useMemo<ColumnDef<Job>[]>(
        () => [
        {
            id: "select",
            header: ({ table }: { table: Table<Job> }) => (
                <div className={"pt-1"}>
                    <DataTableCheckBox
                        {...{
                            checked: table.getIsAllRowsSelected(),
                            indeterminate: table.getIsSomeRowsSelected(),
                            onChange: table.getToggleAllRowsSelectedHandler(),
                        }}
                    />
                </div>
            ),
            cell: ({ row }) => (
                <div className={"pt-1"}>
                    <DataTableCheckBox
                        {...{
                            checked: row.getIsSelected(),
                            disabled: !row.getCanSelect(),
                            indeterminate: row.getIsSomeSelected(),
                            onChange: row.getToggleSelectedHandler(),
                        }}
                    />
                </div>
            ),
            size: 50
        },
            {
                header: "Job ID",
                accessorKey: "id",
                id: "id",
                cell: info => info.getValue()
            },
            {
                accessorFn: row => row.name,
                id: "name",
                cell: info => info.getValue(),
                header: "Name",
            },
            {
                accessorFn: row => row.description,
                id: "description",
                cell: info => info.getValue(),
                header: "Description",
            },
            {
                accessorFn: row => row.status,
                id: "status",
                cell: info => info.getValue(),
                header: "Status"
            },
            {
                accessorFn: row => row.retryCount,
                id: "retryCount",
                cell: info => info.getValue(),
                header: "Retry Count"
            },
            {
              accessorKey: "createdAt",
              id: "createdAt",
              header: "Created At",
              cell: info => {
                  const str = info.getValue() as Date;
                  return str.toLocaleDateString();
              },
              meta: {
                  filterVariant: "date",
              },
              filterFn: (row, columnId, filterValue) => {
                  const columnDate = row.getValue(columnId) as Date;
                  const {from, to} = filterValue;
                  return isWithinInterval(columnDate,{ start: from, end: to || from });
              }
            },
            {
              accessorKey: "updatedAt",
              id: "updatedAt",
              header: "Updated At",
              cell: info => {
                  const str = info.getValue() as Date;
                  return str.toLocaleDateString();
              },
              meta: {
                  filterVariant: "date",
              },
              filterFn: (row, columnId, filterValue) => {
                  const columnDate = row.getValue(columnId) as Date;
                  const {from, to} = filterValue;
                  return isWithinInterval(columnDate,{ start: from, end: to || from });
              }
            }, 
            {
              accessorKey: "startedAt",
              id: "startedAt",
              header: "Started At",
              cell: info => {
                  const str = info.getValue() as Date;
                  return str.toLocaleDateString();
              },
              meta: {
                  filterVariant: "date",
              },
              filterFn: (row, columnId, filterValue) => {
                  const columnDate = row.getValue(columnId) as Date;
                  const {from, to} = filterValue;
                  return isWithinInterval(columnDate,{ start: from, end: to || from });
              }
            },
                        {
              accessorKey: "completedAt",
              id: "completedAt",
              header: "Completed At",
              cell: info => {
                  const str = info.getValue() as Date;
                  return str.toLocaleDateString();
              },
              meta: {
                  filterVariant: "date",
              },
              filterFn: (row, columnId, filterValue) => {
                  const columnDate = row.getValue(columnId) as Date;
                  const {from, to} = filterValue;
                  return isWithinInterval(columnDate,{ start: from, end: to || from });
              }
            }
          ],
        []
    );

    useEffect(()=>{
        const tmo = setTimeout(()=>{
            setLoading(false);
            clearTimeout(tmo);
        },5000);
    },[]);

    return (
		<>
			<AdvancedDataTable<Job>
				id={"job-advance-table"}
				columns={columns}
				data={jobs}
				actionProps={{
				}}
				onRowClick={(prop) => {
					console.log("onRowClick",prop);
				}}
				isLoading={isLoading}
			/>
		</>
	);
}