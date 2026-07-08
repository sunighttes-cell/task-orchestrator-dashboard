import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";

interface AdminRouteProps {
  requiredRole?: "ADMIN" | "USER";
}

export function AdminRoute({ requiredRole = "ADMIN" }: AdminRouteProps) {
  const { auth } = useAuth();
  const { token, user } = auth;

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole === "ADMIN" && user?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
