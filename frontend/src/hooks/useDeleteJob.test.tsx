import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/utils/react-query";
import { useDeleteJob } from "./useDeleteJob";

describe("useDeleteJob", () => {
  it("deletes a job", async () => {
    const { result } = renderHook(() => useDeleteJob(), {
      wrapper: createWrapper(),
    });

    result.current.mutate(1);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});