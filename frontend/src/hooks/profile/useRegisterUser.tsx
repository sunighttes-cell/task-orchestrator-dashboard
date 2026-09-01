import { useMutation } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { registerUser } from "@/auth/api/AuthApi";

export function useRegisterUser() {
  const { register } = useAuth();
  const navigate = useNavigate();

    return useMutation({
    mutationFn: registerUser,
    onSuccess: (data) => {
      // Store the token in sessionStorage or context
      register(data);
      navigate('/dashboard');
      toast.success("User registered successfully");
    }
  });
}
 