export const JobStatus = {
  QUEUED: "QUEUED",
  RUNNING: "RUNNING",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
} as const;

export type JobStatus = (typeof JobStatus)[keyof typeof JobStatus];
export type JobStatusFilter = JobStatus | "ALL";

export interface Job {
  id: number;
  name: string;
  description?: string;
  status: JobStatus;

  createdAt: string;
  updatedAt: string;

  startedAt?: string;
  completedAt?: string;

  retryCount: number;
}

export interface CreateJobRequest {
  name: string;
  description?: string;
}

export interface JobsPageResponse {
  content: Job[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface JobsQueryFilters {
  search?: string;
  status?: JobStatusFilter;
  page?: number;
  size?: number;
}

/*export interface UpdateJobRequest {
  name?: string;
  description?: string;
  status?: JobStatus;
} */ 

export interface StatusSummaryResponse {
  status: JobStatus;
  count: number;
}
