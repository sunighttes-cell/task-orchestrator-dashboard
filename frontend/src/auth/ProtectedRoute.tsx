import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";

export function ProtectedRoute() {
  const { auth } = useAuth();
  const { token} = auth;
  //const location = useLocation();

  if (!token) {
    //state={{ from: location }}
    return <Navigate to="/login" replace/>;
  }

  return <Outlet />;
}