// mutation hook, uses createJob to create new job,invalidate jobs query and summary query ensuring job list and summary are updated after a new job is created. used in the CreateJobForm. abstracts away API logic.

import { useMutation } from "@tanstack/react-query";
import { createJob } from "@/api/jobsApi";
import type { Job, CreateJobRequest } from "@/types/job";

export function useCreateJob() {
  // useMutation hook to handle create job mutation
  const mutation = useMutation<Job, Error, CreateJobRequest>({
    mutationFn: createJob,
  });
    //Returns mutation object: function, state (isLoading, isError, data);
  return mutation;
}

