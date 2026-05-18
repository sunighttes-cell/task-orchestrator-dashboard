import { renderHook, waitFor } from "@testing-library/react";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/utils/react-query";
import { useJobSummary } from "./useJobSummary";
import { createMockSummary } from "@/test/factories/job";

describe("useJobSummary", () => {
  it("returns summary data on success", async () => {
    const mock = createMockSummary();

    server.use(
      http.get("/jobs/status-summary", () =>
        HttpResponse.json([mock])
      )
    );

    const { result } = renderHook(() => useJobSummary(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual([mock]);
  });

  it("returns error state on failure", async () => {
    server.use(
      http.get("/jobs/status-summary", () =>
        new HttpResponse(null, { status: 500 })
      )
    );

    const { result } = renderHook(() => useJobSummary(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.data).toBeUndefined();
  });
});