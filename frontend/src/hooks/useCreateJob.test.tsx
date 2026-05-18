import { renderHook, waitFor } from "@testing-library/react";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";
import { createWrapper } from "@/test/utils/react-query";
import { useCreateJob } from "./useCreateJob";
import { createMockJob } from "@/test/factories/job";

describe("useCreateJob", () => {
  it("creates a job", async () => {
    const mockJob = createMockJob();

    server.use(
      http.post("/jobs", () =>
        HttpResponse.json(mockJob, { status: 201 })
      )
    );

    const { result } = renderHook(() => useCreateJob(), {
      wrapper: createWrapper(),
    });

    result.current.mutate({
      name: "Job 1",
      description: "desc",
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data).toMatchObject(mockJob);
  });
});
