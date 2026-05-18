import { useSearchParams } from "react-router-dom";
import { useMemo } from "react";

export const useJobFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const filters = useMemo(() => {
    return {
      status: searchParams.get("status") || undefined,
      search: searchParams.get("search") || undefined,
      page: Number(searchParams.get("page") || 0),
      size: Number(searchParams.get("size") || 50),
      sort: searchParams.get("sort") || "createdAt,desc",
    };
  }, [searchParams]);

  return { filters, searchParams, setSearchParams };
};