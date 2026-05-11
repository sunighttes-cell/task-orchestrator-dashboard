
//Zustand store for dashboard filters, search state and future sorting/pagination. Used in JobList and SearchBar 

import { create } from "zustand";
import type { JobStatusFilter } from "@/types/job";

interface JobFilterState {
  search: string;
  status: JobStatusFilter;

  setSearch: (value: string) => void;
  setStatus: (value: JobStatusFilter) => void;
  resetFilters: () => void;
}

export const useJobFilterStore = create<JobFilterState>((set) => ({
  search: "",
  status: "ALL",

  setSearch: (value) => set({ search: value }),

  setStatus: (value) => set({ status: value }),

  resetFilters: () =>
    set({
      search: "",
      status: "ALL",
    }),
}));
