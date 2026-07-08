import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/utils/react-query";
import { useRetryJob } from "./useRetryJob";

describe("useRetryJob", () => {
  it("retries a job", async () => {
    const { result } = renderHook(() => useRetryJob(), {
      wrapper: createWrapper(),
    });

    result.current.mutate(1);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});