// mutation hook, uses createJob to create new job,invalidate jobs query and summary query ensuring job list and summary are updated after a new job is created. used in the CreateJobForm. abstracts away API logic.

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createJob } from "@/api/jobsApi";
import { type Job, type CreateJobRequest, JobStatus } from "@/types/job";

interface CreateJobContext {
  previousJobs?: Job[];
}

export function useCreateJob() {
  const queryClient = useQueryClient();

  // useMutation hook to handle create job
  const mutation = useMutation<Job, Error, CreateJobRequest, CreateJobContext>({
    mutationFn: createJob,
    onMutate: async (newJob) => {
      //optimistically update the cache here
        await queryClient.cancelQueries({ queryKey: ["jobs"] });
        const previousJobs = queryClient.getQueryData<Job[]>(["jobs"]);
        if (previousJobs) {
          queryClient.setQueryData<Job[]>(["jobs"], [...previousJobs, { ...newJob, id: Date.now(), status: JobStatus.QUEUED, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), retryCount: 0 }]);
        }
        return { previousJobs };
    },
    onError: (_error, _newJob, context) => {
      //rollback cache update on error
        if (context?.previousJobs) {
          queryClient.setQueryData<Job[]>(["jobs"], context.previousJobs);
        }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
      queryClient.invalidateQueries({ queryKey: ["status-summary"] });
    },
  });
    //Returns mutation object: function, state (isLoading, isError, data);
  return mutation;
}
