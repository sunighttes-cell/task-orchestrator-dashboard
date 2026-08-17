// mutation hook, uses createJob to create new job,invalidate jobs query and summary query ensuring job list and summary are updated after a new job is created. used in the CreateJobForm. abstracts away API logic.

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createJob } from "@/api/jobsApi";
// import { type Job, type CreateJobRequest} from "@/types/job";
import { JobStatus, type JobsPageResponse } from "@/types/job";

export function useCreateJob() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createJob,

    onMutate: async (newJob) => {
      //optimistically update the cache here
        await queryClient.cancelQueries({ queryKey: ["jobs"] });

        const previous = queryClient.getQueriesData({ queryKey: ["jobs"] });

        queryClient.setQueriesData<JobsPageResponse>(
          { queryKey: ["jobs"] },
          (old) => {
            if (!old) {
              return old;
            }

            return {
              ...old,
              content: [
                {
                  id: Date.now(),
                  name: newJob.name,
                  status: JobStatus.QUEUED,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString(),
                  retryCount: 0,
                },
                ...old.content,
              ],
            };
          }
        );
      return { previous };
    },

    onError: (_err, _newJob, context) => {
        context?.previous.forEach(([key, data]) => {
        queryClient.setQueryData(key, data);
      });
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"], exact: false, });
    },

  });
    //Returns mutation object: function, state (isLoading, isError, data);
}
