//Centralize API calls.

import type {
  Job,
  CreateJobRequest,
  JobsPageResponse,
  StatusSummaryResponse,
  JobsQueryFilters,
} from "@/types/job";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

//createJob()
export async function createJob(createJobRequest: CreateJobRequest): Promise<Job> {
  const response = await fetch(`${API_BASE_URL}/jobs`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(createJobRequest),
  });
  if (!response.ok) {
    throw new Error("Failed to create job");
  }
  return response.json();
}

//getJobs()
export async function getJobs(filters: JobsQueryFilters = {}): Promise<Job[]> {
  const params = new URLSearchParams();

  if (filters.search?.trim()) {
    params.set("search", filters.search.trim());
  }
  if (filters.status && filters.status !== "ALL") {
    params.set("status", filters.status);
  }
  if (filters.page !== undefined) {
    params.set("page", String(filters.page));
  }
  if (filters.size !== undefined) {
    params.set("size", String(filters.size));
  }

  const queryString = params.toString();
  const response = await fetch(`${API_BASE_URL}/jobs${queryString ? `?${queryString}` : ""}`);
  if (!response.ok) {
    throw new Error("Failed to fetch jobs");
  }
  const data = (await response.json()) as JobsPageResponse | Job[];
  return Array.isArray(data) ? data : data.content;
}

//retryJob()
export async function retryJob(jobId: number): Promise<Job> {
  const response = await fetch(`${API_BASE_URL}/jobs/${jobId}/retry`, {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("Failed to retry job");
  }
  return response.json();
}

//getStatusSummary()
export async function getStatusSummary(): Promise<StatusSummaryResponse[]> { 
    const response = await fetch(`${API_BASE_URL}/jobs/status-summary`);
    if (!response.ok) {
        throw new Error("Failed to fetch status summary");
    }
    return response.json();
}
