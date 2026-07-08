import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getLoginToken } from "@/api/jobsApi";
import { useAuth } from "@/auth/AuthContext";
import { toast } from "sonner";
import { useNavigate } from 'react-router-dom';

export function useAuthLogin() {
    const { login } = useAuth();
    const navigate = useNavigate();

    return useMutation({
    mutationFn: getLoginToken,
    onSuccess: (data) => {
      // Store the token in sessionStorage or context
      console.log("Login successful, token:", data);
      login(data);
      navigate('/dashboard');
      toast.success("Login successful");
    },
    onError: (error) => {
      console.log("Error: ", error);
      toast.success("Login Failed, Please try again");
    }
  });
}
 