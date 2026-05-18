import {
  useReactTable,
  getCoreRowModel,
  getFilteredRowModel,
  flexRender,
} from "@tanstack/react-table";
import { useState } from "react";
import { PrimaryBtnClass, SecondaryBtnClass } from "@/lib/constants";

export function SimpleDataTable({ columns, data, totalPages, page, handlePageChange }) {
  const [globalFilter, setGlobalFilter] = useState("");

  const table = useReactTable({
    data,
    columns,
    state: {
      globalFilter,
    },
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  });

  return (
    <div className="bg-white dark:bg-gray-900 p-4 rounded-lg border">
      {/* Table */}
      <table className="w-full border">
        <thead>
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <th key={header.id} className="border p-2 text-left">
                  {flexRender(
                    header.column.columnDef.header,
                    header.getContext()
                  )}
                </th>
              ))}
            </tr>
          ))}
        </thead>

        <tbody>
          {table.getRowModel().rows.map((row) => (
            <tr key={row.id} className="border">
              {row.getVisibleCells().map((cell) => (
                <td key={cell.id} className="p-2 border">
                  {flexRender(
                    cell.column.columnDef.cell,
                    cell.getContext()
                  )}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      {/* Pagination */}
      <div className="flex gap-2 mt-4">
        <button
          type="button"
          className={SecondaryBtnClass}
          disabled={page <= 0}
          onClick={() => handlePageChange(page - 1)}
        >
          Prev
        </button>
        <button
          type="button"
          className={PrimaryBtnClass}
          disabled={page >= totalPages - 1}
          onClick={() => handlePageChange(page + 1)}
        >
          Next
        </button>
      </div>
      <footer className="text-sm italic text-gray-500">Showing page {page + 1} of {totalPages} : total of {data.length} jobs </footer>
    </div>
  );
}