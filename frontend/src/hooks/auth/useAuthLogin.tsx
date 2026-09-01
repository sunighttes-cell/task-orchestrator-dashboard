import { useMutation } from "@tanstack/react-query";
import { loginUser } from "@/auth/api/AuthApi";
import { useAuth } from "@/auth/AuthContext";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

type LoginCredentials = {
  username: string;
  password: string;
};

export function useAuthLogin() {
  const { login } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: ({ username, password }: LoginCredentials) =>
      loginUser(username, password),

    onSuccess: (data) => {
      login(data);
      navigate("/dashboard");
      toast.success("Login successful");
    },

    onError: () => {
      toast.error("Login failed. Please check your username and password.");
    },
  });
}