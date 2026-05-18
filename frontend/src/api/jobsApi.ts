import type {
  Job,
  CreateJobRequest,
  StatusSummaryResponse,
  JobsPageResponse
} from "@/types/job";
import { buildQueryParams } from "@/lib/buildQueryParams";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

//Generic request helper
async function request<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
    ...options,
  });

  if (!res.ok) {
    const message = await res.text();
    throw new Error(message || `Request failed: ${res.status}`);
  }

  if (res.status === 204) return {} as T;

  return res.json();
}

// API METHODS

// createJob
export function createJob(
  createJobRequest: CreateJobRequest
): Promise<Job> {
  return request<Job>("/jobs", {
    method: "POST",
    body: JSON.stringify(createJobRequest),
  });
}

// getJobs
export function getJobs(filters: Record<string, any>): Promise<JobsPageResponse> {
  const queryString = buildQueryParams(filters);
  const query = queryString ? `?${queryString}` : "";

  return request<JobsPageResponse>(`/jobs${query}`);
}

// retryJob
export function retryJob(jobId: number): Promise<Job> {
  return request<Job>(`/jobs/${jobId}/retry`, {
    method: "POST",
  });
}

// getStatusSummary
export function getStatusSummary(): Promise<StatusSummaryResponse[]> {
  return request<StatusSummaryResponse[]>("/jobs/status-summary");
}

// deleteJob
export function deleteJob(id: number): Promise<void> {
  return request<void>(`/jobs/${id}`, {
    method: "DELETE",
  });
}