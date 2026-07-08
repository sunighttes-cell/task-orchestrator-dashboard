import { createBrowserRouter } from "react-router-dom";
import AppLayout from "@/layout/AppLayout";
import AuthLayout from "@/layout/AuthLayout";
import DashboardPage from "@/features/dashboard/DashboardPage";
import JobsPage from "@/features/jobs/JobsPage";
import LoginPage from "@/features/profile/LoginPage";
import ProfilePage from "@/features/profile/ProfilePage";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import { PublicRoute } from "@/auth/PublicRoute";
import RegisterForm from "@/features/auth/RegisterForm";

export const router = createBrowserRouter([
  //Public routes
  {
    element: <PublicRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          { path: "/login", element: <LoginPage /> },
          { path: "/register", element: <RegisterForm /> }
        ],
      },
    ],
  },

  //Protected routes
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/", element: <DashboardPage /> },
          { path: "/jobs", element: <JobsPage /> },
          { path: "/profile", element: <ProfilePage /> },
        ],
      },
    ],
  },
]);