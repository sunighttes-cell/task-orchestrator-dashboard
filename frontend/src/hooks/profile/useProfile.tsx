
import { useQuery } from "@tanstack/react-query";
import { getProfile } from "@/api/profileApi";

export function useProfile() {
  return useQuery({
    queryFn: () => getProfile(),
    queryKey: ["profile"],
    staleTime: 0, // Treat as stale immediately so manual refetch works
  });
}