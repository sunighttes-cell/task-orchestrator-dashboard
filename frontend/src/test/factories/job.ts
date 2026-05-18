// src/test/factories/job.ts

import { JobStatus } from "@/types/job";

const MOCK_TIMESTAMP = "2026-01-01T00:00:00.000Z";

export function createMockJob(overrides = {}) {
  return {
    id: 1,
    name: "Job 1",
    description: "desc",
    status: JobStatus.QUEUED,
    retryCount: 0,
    createdAt: MOCK_TIMESTAMP,
    updatedAt: MOCK_TIMESTAMP,
    ...overrides,
  };
}

export function createMockSummary(overrides = {}) {
  return {
    status: JobStatus.COMPLETED,
    count: 2,
    ...overrides,
  };
}

export function createMockMetrics(overrides = {}) {
  return {
    totalJobs: 20,
    completedJobs: 16,
    runningJobs: 2,
    failedJobs: 2,
    successRate: 90.21,
    activeWorkers: 2,
    avgExecutionTime: 2.02,
    ...overrides,
  };
}