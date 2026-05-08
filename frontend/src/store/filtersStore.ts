import { create } from "zustand";

interface FiltersState {
  search: string;
  setSearch: (value: string) => void;
}

export const useFiltersStore = create<FiltersState>((set) => ({
  search: "",
  setSearch: (value) => set({ search: value }),
}));