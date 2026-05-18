// src/test/mocks/handlers.ts

import { http, HttpResponse } from "msw";
import { createMockJob, createMockSummary, createMockMetrics } from "../factories/job";
import { JobStatus } from "@/types/job";

const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const handlers = [
  http.get(`${API}/jobs`, () =>
    HttpResponse.json({
      content: [
        createMockJob(),
        createMockJob({ id: 2, status: JobStatus.RUNNING }),
      ],
      totalElements: 2,
      totalPages: 2,
      size: 50,
      number: 0,
    })
  ),

  http.get(`${API}/jobs/status-summary`, () =>
    HttpResponse.json([
      createMockSummary(),
      createMockSummary({ status: JobStatus.FAILED, count: 1 }),
    ])
  ),

  http.get(`${API}/jobs/metrics`, () =>
    HttpResponse.json(createMockMetrics())
  ),

  http.get(`${API}/jobs/dashboard-metrics`, () =>
    HttpResponse.json(createMockMetrics())
  ),

  http.post(`${API}/jobs`, () =>
    HttpResponse.json(createMockJob())
  ),

  http.post(`${API}/jobs/:id/retry`, () =>
    HttpResponse.json(createMockJob())
  ),

  http.delete(`${API}/jobs/:id`, () =>
    HttpResponse.json({ message: "Job deleted" })
  ),
];
