import { useMutation, useQueryClient } from "@tanstack/react-query";

import { uploadAvatar} from "@/api/profileApi";

export function useUploadAvatar() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: uploadAvatar,
        onSuccess: (profile) => {
            // Immediately update cache with new profile data
            queryClient.setQueryData(["profile"], profile);
            // Also invalidate to ensure fresh data on next fetch
            queryClient.invalidateQueries({queryKey: ["profile"], exact: true});
            console.log("Profile updated successfully in hook:", profile);
            return profile;
        },
        onError: (error) => {
            console.error(error);
        },
    });
}