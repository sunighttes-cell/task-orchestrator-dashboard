import { createContext, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { useJobEvents } from "../hooks/realtime/useJobEvents";
import type { JobEvent, SseConnectionStatus } from "@/types/jobEvents";
import { handleJobEvent } from "@/features/jobs/realtime/jobEventHandlers"

interface JobEventsContextValue {
  status: SseConnectionStatus;
}

const JobEventsContext = createContext<JobEventsContextValue | null>(null);
export function JobEventsProvider({children,}: { children: React.ReactNode;}) {
  const { auth } = useAuth();
  const accessToken = auth.token;
  const queryClient = useQueryClient();

  const handleEvent = useCallback(
  async (event: JobEvent) => {
    await handleJobEvent(event, queryClient);
  },
  [queryClient],
);

  const handleOpen = useCallback(() => {}, []);

  const { status } = useJobEvents({
      accessToken,
      onOpen: handleOpen,
      onEvent: handleEvent,
  });
  
  return (
    <JobEventsContext.Provider value={{ status }} >
      {children}
    </JobEventsContext.Provider>
  );
}