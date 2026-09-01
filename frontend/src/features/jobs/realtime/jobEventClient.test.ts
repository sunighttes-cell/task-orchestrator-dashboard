import { beforeEach,
  afterEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  connectToJobEvents,
} from "./jobEventClient";

import {
  refreshAccessToken,
} from "@/auth/api/AuthApi";

vi.mock("@/auth/api/AuthApi", () => ({
  refreshAccessToken: vi.fn(),
}));

describe("connectToJobEvents", () => {
  beforeEach(() => {
    sessionStorage.clear();

    vi.clearAllMocks();

    vi.unstubAllGlobals();
  });

  afterEach(() => {
    sessionStorage.clear();

    vi.unstubAllGlobals();
  });

  // ===========================================================================
  // 401 -> refresh -> reconnect
  // ===========================================================================

  it(
    "refreshes once and reconnects successfully when SSE returns 401",
    async () => {
      sessionStorage.setItem(
        "refreshToken",
        "refresh-token-a",
      );

      /*
       * refreshAccessToken returns the NEW ACCESS TOKEN.
       *
       * It also updates sessionStorage with the rotated
       * refresh token internally.
       */
      vi.mocked( 
        refreshAccessToken,
      ).mockImplementation(
        async () => {
          sessionStorage.setItem(
            "refreshToken",
            "refresh-token-b",
          );

          sessionStorage.setItem(
            "token",
            "new-access-token",
          );

          return {
            accessToken: "new-access-token",
            refreshToken: "refresh-token-b",
          };
        },
      );

      const fetchMock = vi
        .fn()
        .mockResolvedValueOnce(
          new Response(
            null,
            {
              status: 401,
              statusText:
                "Unauthorized",
            },
          ),
        )
        .mockResolvedValueOnce(
          new Response(
            [
              "event: JOB_UPDATED",
              'data: {"jobId":"job-123","type":"JOB_UPDATED"}',
              "",
              "",
            ].join("\n"),
            {
              status: 200,
              headers: {
                "Content-Type":
                  "text/event-stream",
              },
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onEvent = vi.fn();
      const onOpen = vi.fn();
      const onError = vi.fn();

      await connectToJobEvents({
        url:
          "http://localhost:8080/realtime/jobs",
        accessToken:
          "expired-access-token",
        onEvent,
        onOpen,
        onError,
      });

      // -----------------------------------------------------------------------
      // Refresh
      // -----------------------------------------------------------------------

      expect(
        refreshAccessToken,
      ).toHaveBeenCalledTimes(1);

      expect(
        refreshAccessToken,
      ).toHaveBeenCalledWith(
        "refresh-token-a",
      );

      // -----------------------------------------------------------------------
      // Fetch should have been called twice:
      //
      // 1. expired access token
      // 2. new access token
      // -----------------------------------------------------------------------

      expect(
        fetchMock,
      ).toHaveBeenCalledTimes(2);

      expect(
        fetchMock.mock.calls[0][0],
      ).toBe(
        "http://localhost:8080/realtime/jobs",
      );

      expect(
        fetchMock.mock.calls[0][1],
      ).toMatchObject({
        method: "GET",
        headers: {
          Accept:
            "text/event-stream",
          Authorization:
            "Bearer expired-access-token",
        },
      });

      expect(
        fetchMock.mock.calls[1][1],
      ).toMatchObject({
        method: "GET",
        headers: {
          Accept:
            "text/event-stream",
          Authorization:
            "Bearer new-access-token",
        },
      });

      // -----------------------------------------------------------------------
      // SSE event
      // -----------------------------------------------------------------------

      expect(
        onOpen,
      ).toHaveBeenCalledTimes(1);

      expect(
        onEvent,
      ).toHaveBeenCalledTimes(1);

      expect(
        onEvent.mock.calls[0][0],
      ).toMatchObject({
        jobId: "job-123",
        type: "JOB_UPDATED",
      });

      expect(
        onError,
      ).not.toHaveBeenCalled();
    },
  );

  // ===========================================================================
  // 401 twice
  // ===========================================================================

  it(
    "does not retry more than once when SSE returns 401 twice",
    async () => {
      sessionStorage.setItem(
        "refreshToken",
        "refresh-token",
      );

      vi.mocked(refreshAccessToken).mockResolvedValue({
        accessToken: "new-access-token",
        refreshToken: "refresh-token-b",
      });

      const fetchMock = vi
        .fn()
        .mockResolvedValueOnce(
          new Response(
            null,
            {
              status: 401,
              statusText:
                "Unauthorized",
            },
          ),
        )
        .mockResolvedValueOnce(
          new Response(
            null,
            {
              status: 401,
              statusText:
                "Unauthorized",
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onEvent = vi.fn();
      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "expired-access-token",
          onEvent,
          onError,
        }),
      ).rejects.toMatchObject({
        code: "SSE_UNAUTHORIZED",
        status: 401,
      });

      expect(
        refreshAccessToken,
      ).toHaveBeenCalledTimes(1);

      expect(
        fetchMock,
      ).toHaveBeenCalledTimes(2);

      expect(
        onError,
      ).toHaveBeenCalledTimes(1);
    },
  );

  // ===========================================================================
  // 403
  // ===========================================================================

  it(
    "does not refresh when SSE returns 403",
    async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValue(
          new Response(
            "forbidden",
            {
              status: 403,
              statusText:
                "Forbidden",
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "valid-access-token",
          onEvent: vi.fn(),
          onError,
        }),
      ).rejects.toMatchObject({
        code: "SSE_FORBIDDEN",
        status: 403,
      });

      expect(
        refreshAccessToken,
      ).not.toHaveBeenCalled();

      expect(
        fetchMock,
      ).toHaveBeenCalledTimes(1);

      expect(
        onError,
      ).toHaveBeenCalledTimes(1);
    },
  );

  // ===========================================================================
  // Missing refresh token
  // ===========================================================================

  it(
    "fails with SSE refresh error when 401 occurs and no refresh token exists",
    async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValue(
          new Response(
            null,
            {
              status: 401,
              statusText:
                "Unauthorized",
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "expired-access-token",
          onEvent: vi.fn(),
          onError,
        }),
      ).rejects.toMatchObject({
        code: "SSE_REFRESH_FAILED",
        status: 401,
      });

      expect(
        refreshAccessToken,
      ).not.toHaveBeenCalled();

      expect(
        fetchMock,
      ).toHaveBeenCalledTimes(1);

      expect(
        onError,
      ).toHaveBeenCalledTimes(1);
    },
  );

  // ===========================================================================
  // Other HTTP errors
  // ===========================================================================

  it(
    "throws for non-401/403 SSE HTTP errors",
    async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValue(
          new Response(
            "internal server error",
            {
              status: 500,
              statusText:
                "Internal Server Error",
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "valid-access-token",
          onEvent: vi.fn(),
          onError,
        }),
      ).rejects.toMatchObject({
        code: "SSE_CONNECTION_FAILED",
        status: 500,
      });

      expect(
        refreshAccessToken,
      ).not.toHaveBeenCalled();

      expect(
        onError,
      ).toHaveBeenCalledTimes(1);
    },
  );

  // ===========================================================================
  // Successful connection
  // ===========================================================================

  it(
    "connects successfully and parses SSE events",
    async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValue(
          new Response(
            [
              "event: JOB_UPDATED",
              'data: {"jobId":"job-456","type":"JOB_UPDATED"}',
              "",
              "",
            ].join("\n"),
            {
              status: 200,
              headers: {
                "Content-Type":
                  "text/event-stream",
              },
            },
          ),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onEvent = vi.fn();
      const onOpen = vi.fn();
      const onError = vi.fn();

      await connectToJobEvents({
        url:
          "http://localhost:8080/realtime/jobs",
        accessToken:
          "valid-access-token",
        onEvent,
        onOpen,
        onError,
      });

      expect(
        onOpen,
      ).toHaveBeenCalledTimes(1);

      expect(
        onEvent,
      ).toHaveBeenCalledTimes(1);

      expect(
        onEvent.mock.calls[0][0],
      ).toMatchObject({
        jobId: "job-456",
        type: "JOB_UPDATED",
      });

      expect(
        onError,
      ).not.toHaveBeenCalled();

      expect(
        refreshAccessToken,
      ).not.toHaveBeenCalled();
    },
  );

  // ===========================================================================
  // Abort
  // ===========================================================================

  it(
    "silently stops when the SSE request is aborted",
    async () => {
      const abortError =
        new DOMException(
          "The operation was aborted",
          "AbortError",
        );

      const fetchMock = vi
        .fn()
        .mockRejectedValue(
          abortError,
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "valid-access-token",
          onEvent: vi.fn(),
          onError,
        }),
      ).resolves.toBeUndefined();

      expect(
        onError,
      ).not.toHaveBeenCalled();

      expect(
        refreshAccessToken,
      ).not.toHaveBeenCalled();
    },
  );

  // ===========================================================================
  // Missing response body
  // ===========================================================================

  it(
    "throws when the SSE response has no readable body",
    async () => {
      const fetchMock = vi
        .fn()
        .mockResolvedValue(
          new Response(null, {
            status: 200,
            headers: {
              "Content-Type":
                "text/event-stream",
            },
          }),
        );

      vi.stubGlobal(
        "fetch",
        fetchMock,
      );

      const onError = vi.fn();

      await expect(
        connectToJobEvents({
          url:
            "http://localhost:8080/realtime/jobs",
          accessToken:
            "valid-access-token",
          onEvent: vi.fn(),
          onError,
        }),
      ).rejects.toMatchObject({
        code: "SSE_NO_BODY",
      });

      expect(
        onError,
      ).toHaveBeenCalledTimes(1);
    },
  );
});