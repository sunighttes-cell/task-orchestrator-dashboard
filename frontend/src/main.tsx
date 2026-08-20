import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { RouterProvider } from "react-router-dom";
import { router } from "@/router";
import { ThemeProvider } from "./providers/ThemeProvider";
import "./index.css";
import { AuthProvider } from "./auth/AuthContext";
import { JobEventsProvider } from "@/providers/JobEventsProvider";


const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
      <QueryClientProvider client={queryClient}>
          <ThemeProvider>
          <AuthProvider>
            <JobEventsProvider>
              <RouterProvider router={router} />
            </JobEventsProvider>
          </AuthProvider>
        </ThemeProvider>
      </QueryClientProvider>
  </React.StrictMode>
);
