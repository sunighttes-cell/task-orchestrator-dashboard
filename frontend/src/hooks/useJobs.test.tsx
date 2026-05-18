import { renderHook, waitFor } from "@testing-library/react";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/utils/react-query";
import { useJobs } from "./useJobs";
import { createMockJob } from "@/test/factories/job";

describe("useJobs", () => {
  it("returns jobs data with filters", async () => {
    const mockJobs = [
      createMockJob({ id: 1, status: "COMPLETED" }),
      createMockJob({ id: 2, status: "FAILED" }),
    ];

    server.use(
      http.get("/jobs", () =>
        HttpResponse.json({
          content: mockJobs,
          totalElements: 2,
          totalPages: 1,
          size: 10,
          number: 0,
        })
      )
    );

    const { result } = renderHook(
      () => useJobs({ status: "FAILED", page: 0, size: 10 }),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data?.content).toEqual(mockJobs);
  });

  it("returns error state on failure", async () => {
    server.use(
      http.get("/jobs", () =>
        new HttpResponse(null, { status: 500 })
      )
    );

    const { result } = renderHook(() => useJobs({}), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
  });

  it("starts in loading state", () => {
    const { result } = renderHook(() => useJobs({}), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
  });
});