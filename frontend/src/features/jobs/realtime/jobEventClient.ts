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

export async function connectToJobEvents({ url, accessToken, onEvent, onOpen, onError,
  signal,
}: JobEventClientOptions): Promise<void> {

  let currentAccessToken = accessToken;
  let retryCount = 0;

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

      if (!response.ok) {
        if (response.status === 401 && retryCount === 0) {
          const refreshToken = sessionStorage.getItem("refreshToken");
          if (!refreshToken) {
            throw new Error("SSE refresh failed: missing refresh token");
          }

          retryCount += 1;
          currentAccessToken = await refreshAccessToken(refreshToken);
          continue;
        }

        if (response.status === 403) {
          const body = await response.text();
          const forbiddenError = new Error(`SSE forbidden: ${response.status} ${body}`) as Error & { code?: string };
          forbiddenError.code = "SSE_FORBIDDEN";
          throw forbiddenError;
        }

        const body = await response.text();
        console.error({status: response.status, body,});
        throw new Error(`SSE connection failed: ${response.status} ${body}`,);
      }

      onOpen?.();

      if (!response.body) {
        throw new Error("SSE response does not contain a readable body");
      } else console.log("SSE connected");

      const reader = response.body.getReader();
      const decoder = new TextDecoder();

      let buffer = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          break;
        }
        buffer += decoder.decode(value, {
          stream: true,
        });

        const events = buffer.split("\n\n");
        buffer = events.pop() ?? "";

        for (const rawEvent of events) {
          const event = parseSseEvent(rawEvent);
          console.log("SSE event received", event);

          if (event) {
            onEvent(event);
          }
        }
      }

      return;
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") {
        return;
      }

      onError?.(error);

      throw error;
    }
  }
}