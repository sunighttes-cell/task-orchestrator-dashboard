import { useSearchParams } from "react-router-dom";
import { useMemo } from "react";

export const useJobFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const filters = useMemo(() => {
    const pageParam = Number(searchParams.get("page"));
    const sizeParam = Number(searchParams.get("size"));

    return {
      status: searchParams.get("status") || undefined,
      search: searchParams.get("search") || undefined,
      page: Number.isFinite(pageParam) && pageParam >= 0
        ? pageParam
        : 0,
      size: Number.isFinite(sizeParam) && sizeParam > 0
        ? sizeParam
        : 50,
      sort: searchParams.get("sort") || "createdAt,desc",
    };
  }, [searchParams]);

  return {
    filters,
    searchParams,
    setSearchParams,
  };
};

// import { useSearchParams } from "react-router-dom";
// import { useMemo } from "react";

// export const useJobFilters = () => {
//   const [searchParams, setSearchParams] = useSearchParams();

//   const filters = useMemo(() => {
//     return {
//       status: searchParams.get("status") || undefined,
//       search: searchParams.get("search") || undefined,
//       page: Number(searchParams.get("page") || 0),
//       size: Number(searchParams.get("size") || 50),
//       sort: searchParams.get("sort") || "createdAt,desc",
//     };
//   }, [searchParams]);

//   return { filters, searchParams, setSearchParams };
// };