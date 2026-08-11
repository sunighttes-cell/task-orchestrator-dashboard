import { createContext, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { useJobEvents } from "../hooks/realtime/useJobEvents";
import type { JobEvent, SseConnectionStatus } from "@/types/jobEvents";
import { handleJobEvent } from "@/features/jobs/realtime/jobEventHandlers"

// interface JobEventsContextValue {
//   connection: boolean;
// }

interface JobEventsContextValue {
  status: SseConnectionStatus;
}

const JobEventsContext = createContext<JobEventsContextValue | null>(null);
export function JobEventsProvider({children,}: { children: React.ReactNode;}) {
  const { auth } = useAuth();
  const accessToken = auth.token;
  const queryClient = useQueryClient();

  console.log("JobEventsProvider", { accessToken, hasToken: Boolean(accessToken)});
  console.log("1. JobEventsProvider rendered", {hasToken: Boolean(accessToken)});

  const handleEvent = useCallback(
  async (event: JobEvent) => {
    console.log("Job event received", event);

    await handleJobEvent(event, queryClient);
  },
  [queryClient],
);

  // const { status } = useJobEvents({ 
  //   accessToken, 
  //   onOpen: () => undefined, 
  //   onEvent: handleEvent,
  // });

  const handleOpen = useCallback(() => {}, []);

  const { status } = useJobEvents({
      accessToken,
      onOpen: handleOpen,
      onEvent: handleEvent,
  });
  
  console.log("SSE status", status);

  return (
    <JobEventsContext.Provider value={{ status }} >
      {children}
    </JobEventsContext.Provider>
  );
}