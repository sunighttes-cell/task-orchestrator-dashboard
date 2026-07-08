import { http, HttpResponse } from "msw";
import {
  createMockJob,
  createMockSummary,
  createMockMetrics,
} from "../factories/job";
import { createMockProfile } from "../factories/profile";
import { JobStatus } from "@/types/job";
import type { UserProfile } from "@/types/profile";

const STATUSES = [JobStatus.COMPLETED, JobStatus.RUNNING, JobStatus.FAILED];

function buildInitialJobs() {
  // Enough rows so the default page size (50) yields multiple pages
  return Array.from({ length: 60 }, (_, i) =>
    createMockJob({
      id: i + 1,
      name: `Job ${i + 1}`,
      status: STATUSES[i % STATUSES.length],
    })
  );
}

let jobs = buildInitialJobs();
let mockProfile = createMockProfile();

export function resetMockJobs() {
  jobs = buildInitialJobs();
}

export function resetMockProfile() {
  mockProfile = createMockProfile();
}

export function updateMockProfile(profile: Partial<UserProfile>) {
  mockProfile = { ...mockProfile, ...profile };
}

//filter + paginate
function getFilteredJobs(url: URL) {
  const status = url.searchParams.get("status");
  const page = Number(url.searchParams.get("page") ?? 0);
  const size = Number(url.searchParams.get("size") ?? 10);
  const sort = url.searchParams.get("sort");

  let filtered = [...jobs];

  // filter
  if (status) {
    filtered = filtered.filter((j) => j.status === status);
  }

  // sort (basic example)
  if (sort === "status,desc") {
    filtered.sort((a, b) => b.status.localeCompare(a.status));
  }

  // pagination
  const start = page * size;
  const paginated = filtered.slice(start, start + size);

  return {
    content: paginated,
    totalElements: filtered.length,
    totalPages: Math.ceil(filtered.length / size),
    size,
    number: page,
  };
}

export const handlers = [
  //get jobs
  http.get("/jobs", ({ request }) => {
    const url = new URL(request.url);
    return HttpResponse.json(getFilteredJobs(url));
  }),

  //data summary
  http.get("/jobs/status-summary", () => {
    return HttpResponse.json([
      createMockSummary({ status: JobStatus.COMPLETED, count: 1 }),
      createMockSummary({ status: JobStatus.RUNNING, count: 1 }),
      createMockSummary({ status: JobStatus.FAILED, count: 1 }),
    ]);
  }),
  //metrics
  http.get("/jobs/metrics", () => {
    return HttpResponse.json(createMockMetrics());
  }),
  http.get("/jobs/dashboard-metrics", () => {
    return HttpResponse.json(createMockMetrics());
  }),
  //create job
  http.post("/jobs", async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;

    const newJob = createMockJob({
      id: jobs.length + 1,
      ...body,
    });

    jobs.push(newJob);

    return HttpResponse.json(newJob, { status: 201 });
  }),
  //retry
  http.post("/jobs/:id/retry", ({ params }) => {
    const id = Number(params.id);

    const job = jobs.find((j) => j.id === id);

    if (!job) {
      return new HttpResponse(null, { status: 404 });
    }

    const updated = { ...job, status: JobStatus.QUEUED };

    jobs = jobs.map((j) => (j.id === id ? updated : j));

    return HttpResponse.json(updated);
  }),

  //delete
  http.delete("/jobs/:id", ({ params }) => {
    const id = Number(params.id);

    jobs = jobs.filter((j) => j.id !== id);

    return new HttpResponse(null, { status: 204 });
  }),

  // profile endpoints
  http.get("/profile", () => {
    return HttpResponse.json(mockProfile);
  }),

  http.put("/profile", async ({ request }) => {
    const body = (await request.json()) as Record<string, unknown>;
    mockProfile = { ...mockProfile, ...body };
    return HttpResponse.json(mockProfile);
  }),

  http.post("/profile/avatar", async ({ request }) => {
    // Simulate file upload by creating a data URL
    const formData = await request.formData();
    const file = formData.get("file") as File;

    if (!file) {
      return HttpResponse.json(
        { error: "No file provided" },
        { status: 400 }
      );
    }

    // In real scenario, backend would save file and return URL
    // For testing, we create a data URL
    const arrayBuffer = await file.arrayBuffer();
    const blob = new Blob([arrayBuffer], { type: file.type });
    const url = URL.createObjectURL(blob);

    mockProfile = { ...mockProfile, profilePictureUrl: url };
    return HttpResponse.json(mockProfile, { status: 200 });
  }),
];
