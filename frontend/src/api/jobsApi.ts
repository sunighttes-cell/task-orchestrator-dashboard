//Centralize API calls.

import type {
  Job,
  CreateJobRequest,
  StatusSummaryResponse,
  JobsPageResponse
} from "@/types/job";
import {buildQueryParams} from "@/lib/buildQueryParams"

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
export async function getJobs(filters): Promise<JobsPageResponse> {
  const queryString = buildQueryParams(filters);
  console.log("queryString", queryString);

  const res = await fetch(`${API_BASE_URL}/jobs?${queryString}`);
  console.log("res", res);

  if (!res.ok) throw new Error("Failed to fetch jobs");

  return res.json();
};

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

//delete job
export const deleteJob = async (id: number) => {
  const response = await fetch(`${API_BASE_URL}/jobs/${id}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error("Failed to delete job");
  }
};
