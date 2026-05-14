//implement dropdown filter for job status
import { useJobFilterStore } from "@/store/useJobFilterStore";
import type { JobStatusFilter as JobStatusFilterValue } from "@/types/job";

export function JobStatusFilter() {
  const status = useJobFilterStore((s) => s.status);
  const setStatus = useJobFilterStore((s) => s.setStatus);

  return (
    <select
      value={status}
      onChange={(e) => setStatus(e.target.value as JobStatusFilterValue)}
      className="border rounded px-3 py-2"
    >
      <option value="ALL">All</option>
      <option value="QUEUED">Queued</option>
      <option value="RUNNING">Running</option>
      <option value="COMPLETED">Completed</option>
      <option value="FAILED">Failed</option>
    </select>
  );
}