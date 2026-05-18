import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/utils/react-query";
import { useJobs } from "./useJobs";

describe("useJobs", () => {
  it("returns jobs list", async () => {
    const { result } = renderHook(() => useJobs({}), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data.content).toHaveLength(2);
  });
});