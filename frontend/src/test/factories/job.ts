// src/test/factories/job.ts

import { refreshAccessToken } from "@/auth/api/AuthApi";
import { JobStatus } from "@/types/job";

const MOCK_TIMESTAMP = "2026-01-01T00:00:00.000Z";
const MOCK_PROFILE_PIC_URL = "https://example.com/avatar.jpg"

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

export function createMockUser(overrides = {}) {
  return {
    username: "newUser",
    email: "newUser@taskapp.local",
    fullName: "New User",
    password: "stringPw1234!",
    ...overrides,
  };
}

export function createMockProfile(overrides = {}) {
    return {
    id: 1,
    username: "newUser",
    role: "USER",
    fullName: "NEW USER",
    email: "newUser@taskapp.local",
    profilePictureUrl: MOCK_PROFILE_PIC_URL,
    ...overrides,
  };
}

export function createMockAccessData(overrides = {}){
  return {
    accessToken:"mockaccesstoken",
    refreshToken: "mockrefreshtoken",
    ...overrides,
  }
}