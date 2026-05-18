import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deleteJob } from "@/api/jobsApi";
import { toast } from "sonner";

export const useDeleteJob = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteJob,

    onSuccess: () => {
      // refresh jobs table + summary cards + dashboard metrics
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
      queryClient.invalidateQueries({ queryKey: ["status-summary"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-metrics"] });
      toast.success("Job deleted");
    },
  });
};