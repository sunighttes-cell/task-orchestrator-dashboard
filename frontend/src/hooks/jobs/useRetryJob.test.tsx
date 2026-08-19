import { act, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { describe, expect, it, vi } from "vitest";
import { useRetryJob } from "./useRetryJob";
import * as jobsApi from "@/api/jobsApi";

vi.mock("@/api/jobsApi", () => ({
  retryJob: vi.fn(),
}));

describe("useRetryJob", () => {
  it("invalidates jobs and dashboard queries after successful retry", async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");
    vi.mocked(jobsApi.retryJob);

    const { result } = renderHook(() => useRetryJob(), { wrapper });

    await act(async () => {
      await result.current.mutateAsync(1);
    });

    expect(jobsApi.retryJob).toHaveBeenCalled();
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["jobs"], exact: false });
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["status-summary"], exact: false });
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: ["dashboard-metrics"], exact: false });
  });
});