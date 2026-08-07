import { beforeEach, describe, expect, it, vi } from "vitest";
import { connectToJobEvents } from "./jobEventClient";
import { refreshAccessToken } from "@/auth/api/AuthApi";

vi.mock("@/auth/api/AuthApi", () => ({
  refreshAccessToken: vi.fn(),
}));

describe("connectToJobEvents", () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  it("refreshes once and succeeds when the SSE endpoint responds with 401", async () => {
    sessionStorage.setItem("refreshToken", "refresh-token");
    vi.mocked(refreshAccessToken).mockResolvedValue("fresh-access-token");

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(null, { status: 401, statusText: "Unauthorized" })
      )
      .mockResolvedValueOnce(
        new Response(
          "event: JOB_UPDATED\ndata: {\"jobId\":\"job-123\",\"type\":\"JOB_UPDATED\"}\n\n",
          {
            status: 200,
            headers: { "Content-Type": "text/event-stream" },
          }
        )
      );

    vi.stubGlobal("fetch", fetchMock);

    const onEvent = vi.fn();

    await connectToJobEvents({
      url: "http://localhost:8080/realtime/jobs",
      accessToken: "expired-token",
      onEvent,
    });

    expect(refreshAccessToken).toHaveBeenCalledWith("refresh-token");
    expect(onEvent).toHaveBeenCalledTimes(1);
    expect(onEvent.mock.calls[0][0]).toMatchObject({
      jobId: "job-123",
      type: "JOB_UPDATED",
    });
  });

  it("does not retry forever when the backend rejects the SSE connection with 403", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response("forbidden", { status: 403, statusText: "Forbidden" })
      )
    );

    await expect(
      connectToJobEvents({
        url: "http://localhost:8080/realtime/jobs",
        accessToken: "valid-token",
        onEvent: vi.fn(),
      })
    ).rejects.toMatchObject({
      message: expect.stringContaining("SSE forbidden"),
    });

    expect(refreshAccessToken).not.toHaveBeenCalled();
  });
});
