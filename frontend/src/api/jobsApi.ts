//Centralize API calls.

import type { Job, CreateJobRequest, StatusSummaryResponse } from "@/types/job";

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
export async function getJobs(): Promise<Job[]> {
  const response = await fetch(`${API_BASE_URL}/jobs`);
  if (!response.ok) {
    throw new Error("Failed to fetch jobs");
  }
  return response.json();
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