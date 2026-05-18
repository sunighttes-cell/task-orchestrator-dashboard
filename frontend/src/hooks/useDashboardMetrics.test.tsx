import { renderHook, waitFor } from "@testing-library/react";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/utils/react-query";
import { useDashboardMetrics } from "./useDashboardMetrics";
import { createMockMetrics } from "@/test/factories/job";

describe("useDashboardMetrics", () => {
  it("returns metrics data on success", async () => {
    const mock = createMockMetrics();

    server.use(
      http.get("/jobs/dashboard-metrics", () =>
        HttpResponse.json(mock)
      )
    );

    const { result } = renderHook(() => useDashboardMetrics(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mock);
  });

  it("returns error state on failure", async () => {
    server.use(
      http.get("/jobs/dashboard-metrics", () =>
        new HttpResponse(null, { status: 500 })
      )
    );

    const { result } = renderHook(() => useDashboardMetrics(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });
  });

  it("starts in loading state", () => {
    const { result } = renderHook(() => useDashboardMetrics(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
  });
});