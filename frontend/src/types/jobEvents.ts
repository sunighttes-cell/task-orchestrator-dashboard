import type { JobStatus } from "@/types/job"

export type JobEventType =
  | "JOB_CREATED"
  | "JOB_STARTED"
  | "JOB_COMPLETED"
  | "JOB_FAILED"
  | "JOB_RETRIED";

export interface JobEvent {
  jobId: string;
  username: string;
  eventType: JobEventType;
  jobStatus: JobStatus;
}

export type SseConnectionStatus =
  | "DISCONNECTED"
  | "CONNECTING"
  | "CONNECTED"
  | "RECONNECTING"
  | "ERROR";