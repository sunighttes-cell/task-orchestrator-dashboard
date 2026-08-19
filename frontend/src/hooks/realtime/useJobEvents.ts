import { useEffect, useState } from "react";
import { connectToJobEvents } from "@/features/jobs/realtime/jobEventClient";
import type {
  JobEvent,
  SseConnectionStatus,
} from "@/types/jobEvents";
import {
  getReconnectDelay,
  sleep,
} from "@/lib/realtimeConnectionDelays";

interface UseJobEventsOptions {
  accessToken: string | null;
  onEvent: (event: JobEvent) => void;
  onOpen?: () => void;
}

interface UseJobEventsResult {
  status: SseConnectionStatus;
}

export function useJobEvents({
  accessToken,
  onEvent,
  onOpen,
}: UseJobEventsOptions): UseJobEventsResult {
  const [status, setStatus] =
    useState<SseConnectionStatus>("DISCONNECTED");

  const realtimeUrl = `${import.meta.env.VITE_API_BASE_URL ?? ""}/realtime/jobs`;

  useEffect(() => {
    if (!accessToken) {
      return;
    }

    console.log("2. useJobEvents effect started");

    const controller = new AbortController();

    const connectWithRetry = async () => {
      console.log("3. Starting SSE retry loop");

      let attempt = 0;

      while (!controller.signal.aborted) {
        try {
          setStatus(
            attempt === 0
              ? "CONNECTING"
              : "RECONNECTING",
          );

          console.log(
            "4. Calling SSE endpoint",
            realtimeUrl,
          );

          await connectToJobEvents({
            url: realtimeUrl,
            accessToken,
            signal: controller.signal,

            onOpen: () => {
              console.log("SSE connection opened");

              setStatus("CONNECTED");

              onOpen?.();

              attempt = 0;
            },

            onEvent,
          });

          if (controller.signal.aborted) {
            return;
          }

          setStatus("RECONNECTING");

          const delay = getReconnectDelay(attempt);

          await sleep(delay);

          attempt++;
        } catch (error) {
          if (controller.signal.aborted) {
            return;
          }

          if (
            error instanceof Error &&
            (
              error as Error & { code?: string }
            ).code === "SSE_FORBIDDEN"
          ) {
            setStatus("DISCONNECTED");
            return;
          }

          console.error(
            "SSE connection failed",
            error,
          );

          setStatus("RECONNECTING");

          const delay = getReconnectDelay(attempt);

          await sleep(delay);

          attempt++;
        }
      }
    };

    void connectWithRetry();

    return () => {
      console.log("Cleaning up SSE effect");

      controller.abort();
    };
  }, [
    accessToken,
    onEvent,
    onOpen,
    realtimeUrl,
  ]);

  //When there is no access token, the connection is logically
  //disconnected regardless of the previous connection state.
  if (!accessToken) {
    return {
      status: "DISCONNECTED",
    };
  }

  return {
    status,
  };
}