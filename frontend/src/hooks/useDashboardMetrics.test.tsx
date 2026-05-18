import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/utils/react-query";
import { useDashboardMetrics } from "./useDashboardMetrics";
import { createMockMetrics } from "@/test/factories/job";

describe("useDashboardMetrics", () => {
  it("returns metrics data", async () => {
    const { result } = renderHook(() => useDashboardMetrics(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toEqual(createMockMetrics());
  });
});