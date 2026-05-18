import type { JobStatus } from "@/types/job";

export function normalizeStatus(status: string): JobStatus {
  return status.trim().toUpperCase() as JobStatus;
}

export function normalizeName(name: string) {
  return name.trim().toUpperCase();
}
