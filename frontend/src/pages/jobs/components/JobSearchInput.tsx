//implement search input 

import { useJobFilterStore } from "@/store/useJobFilterStore";

export function JobSearchInput() {
  const search = useJobFilterStore((s) => s.search);
  const setSearch = useJobFilterStore((s) => s.setSearch);

  return (
    <input
      type="text"
      placeholder="Search jobs..."
      value={search}
      onChange={(e) => setSearch(e.target.value)}
      className="border rounded px-3 py-2 w-full"
    />
  );
}
