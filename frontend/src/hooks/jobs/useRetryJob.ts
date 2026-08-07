import { useMutation, useQueryClient } from "@tanstack/react-query";
import { retryJob } from "@/api/jobsApi";

export function useRetryJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: retryJob,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"], exact: false });
      queryClient.invalidateQueries({ queryKey: ["status-summary"], exact: false });
      queryClient.invalidateQueries({ queryKey: ["dashboard-metrics"], exact: false });
    },
  });
}