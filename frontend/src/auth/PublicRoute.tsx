import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";

export function PublicRoute() {
  const { auth } = useAuth();
  const { token} = auth;

  if (token) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}