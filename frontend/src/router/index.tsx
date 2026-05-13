import { createBrowserRouter } from "react-router-dom";
import AppLayout from "@/layout/AppLayout";
import DashboardPage from "@/pages/dashboard/DashboardPage";
import JobsPage from "@/pages/jobs/JobsPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: "jobs", element: <JobsPage /> },
    ],
  },
]);
