import { useMutation } from "@tanstack/react-query";
import { updateProfile } from "@/api/profileApi";
import { useQueryClient } from "@tanstack/react-query";
import type { UpdateUserProfileRequest } from "@/types/profile";

export function useProfileUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (profileData: UpdateUserProfileRequest) => updateProfile(profileData),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["profile"] });
        },
        onError: (error) => {
            console.error("Error updating profile:", error);
        },
  });
}