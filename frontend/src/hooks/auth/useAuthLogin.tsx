import { useMutation } from "@tanstack/react-query";
import { getLoginToken } from "@/api/jobsApi";
import { useAuth } from "@/auth/AuthContext";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

export function useAuthLogin() {
  const { login } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: getLoginToken,

    onSuccess: (data) => {
      login(data);
      navigate("/dashboard");
      toast.success("Login successful");
    },

    onError: () => {
      toast.error(
        "Login failed. Please check your username and password."
      );
    },
  });
}