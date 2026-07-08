import { useMutation } from "@tanstack/react-query";
import { updatePassword } from "@/api/profileApi";
import { useQueryClient } from "@tanstack/react-query";
import type { UpdatePasswordRequest } from "@/types/profile";

export function usePasswordUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (passwordData: UpdatePasswordRequest) => updatePassword(passwordData),
        onSuccess: (data) => {
            console.log("Password updated successfully:", data);
            queryClient.invalidateQueries({ queryKey: ["profile"] });
        },
        onError: (error) => {
            console.error("Error updating password:", error);
        },
  });
}