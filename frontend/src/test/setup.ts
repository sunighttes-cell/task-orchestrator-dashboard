import "@testing-library/jest-dom";
import { server } from "@/test/mock/server";
import { beforeAll, afterAll, afterEach, vi } from "vitest";

//hook server into test lifecycle
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

beforeEach(() => {
  // Spies on console.error and silences it
  vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  vi.restoreAllMocks();
});
