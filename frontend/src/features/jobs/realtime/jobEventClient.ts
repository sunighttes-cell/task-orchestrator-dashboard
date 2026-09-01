import type { JobEvent } from "@/types/jobEvents";
import { refreshAccessToken } from "@/auth/api/AuthApi";
import parseSseEvent from "./jobEventParser";

interface JobEventClientOptions {
  url: string;
  accessToken: string;
  onEvent: (event: JobEvent) => void;
  onOpen?: () => void;
  onError?: (error: unknown) => void;
  signal?: AbortSignal;
}

type SseError = Error & {
  code?: string;
  status?: number;
};

function createSseError(
  message: string,
  code?: string,
  status?: number,
): SseError {
  const error = new Error(message) as SseError;

  if (code) {
    error.code = code;
  }

  if (status !== undefined) {
    error.status = status;
  }

  return error;
}

export async function connectToJobEvents({
  url,
  accessToken,
  onEvent,
  onOpen,
  onError,
  signal,
}: JobEventClientOptions): Promise<void> {
  let currentAccessToken = accessToken;
  let hasRetriedAfter401 = false;

  while (true) {
    try {
      const response = await fetch(url, {
        method: "GET",
        headers: {
          Accept: "text/event-stream",
          Authorization: `Bearer ${currentAccessToken}`,
        },
        signal,
      });

      // Unauthorized: Access token may have expired. Get current refresh token
      // from sessionStorage and let refreshAccessToken() handle:
      // POST /auth/refresh: old refresh token -> revoked
      // Only one refresh attempt is allowed for this SSE connection.
      if (response.status === 401) {
        if (hasRetriedAfter401) {
          throw createSseError(
            "SSE connection unauthorized after token refresh",
            "SSE_UNAUTHORIZED",
            401,
          );
        }

        const refreshToken = sessionStorage.getItem("refreshToken");
        if (!refreshToken) {
          throw createSseError(
            "SSE refresh failed: missing refresh token",
            "SSE_REFRESH_FAILED",
            401,
          );
        }

        hasRetriedAfter401 = true;
        const refreshed =  await refreshAccessToken(refreshToken);
        currentAccessToken = refreshed.accessToken;
        continue;
      }

      // Forbidden: 403 means authentication may be valid but user does not have
      // permission to access the SSE endpoint. Do NOT attempt token refresh.
      if (response.status === 403) {
        const body = await response.text();
        throw createSseError(
          `SSE forbidden: ${response.status} ${body}`,
          "SSE_FORBIDDEN",
          403,
        );
      }

      // Other HTTP errors
      if (!response.ok) {
        const body = await response.text();
        throw createSseError(
          `SSE connection failed: ${response.status} ${body}`,
          "SSE_CONNECTION_FAILED",
          response.status,
        );
      }

      // Successful connection
      onOpen?.();
      if (!response.body) {
        throw createSseError(
          "SSE response does not contain a readable body",
          "SSE_NO_BODY",
        );
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";

      // Read SSE stream
      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          break;
        }
        buffer += decoder.decode(value, {
          stream: true,
        });
        const events = buffer.split("\n\n");
        // Keep the incomplete event for the next chunk.
        buffer = events.pop() ?? "";
        for (const rawEvent of events) {
          const event = parseSseEvent(rawEvent);
          if (event) {
            console.log(
              "SSE event received",
              event,
            );
            onEvent(event);
          }
        }
      }

      // Flush any remaining decoder bytes.
      buffer += decoder.decode();

      // Process a final event if the server closed the connection without
      // leaving the normal "\n\n" delimiter.
      if (buffer.trim()) {
        const event = parseSseEvent(buffer);
        if (event) {
          console.log(
            "SSE event received",
            event,
          );
          onEvent(event);
        }
      }

      // The stream closed normally.
      return;
    } catch (error) {
      // AbortController cancellation is an expected shutdown condition.
      if (
        error instanceof DOMException &&
        error.name === "AbortError"
      ) {
        return;
      }
      onError?.(error);
      throw error;
    }
  }
}