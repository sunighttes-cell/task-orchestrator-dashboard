import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";

export function ProtectedRoute() {
  const { auth } = useAuth();
  const { token} = auth;

  if (!token) {
    return <Navigate to="/login" replace/>;
  }

  return <Outlet />;
}