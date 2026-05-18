import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "@/test/utils/react-query";
import { useCreateJob } from "./useCreateJob";
import { createMockJob } from "@/test/factories/job";

describe("useCreateJob", () => {
  it("creates a job", async () => {
    const { result } = renderHook(() => useCreateJob(), {
      wrapper: createWrapper(),
    });

    result.current.mutate({
      name: "Job 1",
      description: "desc",
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toMatchObject(createMockJob());
  });
});