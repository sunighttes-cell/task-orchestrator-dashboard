import "@testing-library/jest-dom";
import { server } from "@/test/mock/server";
import { resetMockJobs } from "@/test/mock/handlers";
import { beforeAll, afterAll, afterEach, vi } from "vitest";

// Use relative URLs in tests so MSW handlers (e.g. "/jobs") match.
// jsdom resolves them against http://localhost, and MSW matches by pathname.
vi.stubEnv("VITE_API_BASE_URL", "");

//hook server into test lifecycle
beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
afterEach(() => {
  server.resetHandlers();
  resetMockJobs();
});
afterAll(() => server.close());

beforeEach(() => {
  // Spies on console.error and silences it
  vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  vi.restoreAllMocks();
});
