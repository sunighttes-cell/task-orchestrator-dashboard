
//Zustand store for dashboard filters, search state and future sorting/pagination state. Used in JobList and SearchBar 

import { create } from "zustand";

type JobStatus = "ALL" | "QUEUED" | "RUNNING" | "SUCCESS" | "FAILED";

interface JobFilterState {
  search: string;
  status: JobStatus;

  setSearch: (value: string) => void;
  setStatus: (value: JobStatus) => void;
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