import type {
  Job,
  CreateJobRequest,
  StatusSummaryResponse,
  JobsPageResponse,
} from "@/types/job";
import apiClient from "./client";
import type { JobFilters } from "@/hooks/jobs/useJobs";

// getLoginToken
export async function getLoginToken(
  loginData: Record<string, string>
): Promise<{ accessToken: string, refreshToken: string }> {
  const res = await apiClient.post("/auth/login", loginData);
  return res.data;
}

//get refresh token
export async function getRefreshToken(
  refreshToken: string
): Promise<{ accessToken: string, refreshToken: string }>  {
  const res = await apiClient.post("/auth/refresh", { refreshToken });
  return res.data;
}

// createJob
export async function createJob(
  createJobRequest: CreateJobRequest
): Promise<Job> {
  const res = await apiClient.post("/jobs", createJobRequest);
  return res.data;
}

// getJobs
//Axios handles encoding safely. We can pass the filters as an object to the params option, and Axios will take care of encoding them properly. This way, we avoid any issues with special characters in the filter values.
export async function getJobs(
  filters: JobFilters
): Promise<JobsPageResponse> {
  const res = await apiClient.get("/jobs", { params: filters });
  return res.data;
}

// retryJob
export async function retryJob(jobId: number): Promise<Job> {
  const res = await apiClient.post(`/jobs/${jobId}/retry`);
  return res.data;
}

// getStatusSummary
export async function getStatusSummary(): Promise<StatusSummaryResponse[]> {
  const res = await apiClient.get("/jobs/status-summary");
  return res.data;
}

// deleteJob
export async function deleteJob(id: number): Promise<void> {
  await apiClient.delete(`/jobs/${id}`);
}