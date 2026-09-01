import { useMutation } from "@tanstack/react-query";
import { updatePassword } from "@/api/profileApi";
import { useQueryClient } from "@tanstack/react-query";
import type { UpdatePasswordRequest } from "@/types/profile";
import {toast} from "sonner";

export function usePasswordUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (passwordData: UpdatePasswordRequest) => updatePassword(passwordData),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["profile"] });
        },
        onError: (error) => {
            console.log("Error updating password: ", error)
            toast.error("Error updating password");
        },
  });
}