import { useMutation, useQueryClient } from "@tanstack/react-query";

import { uploadAvatar} from "@/api/profileApi";
import { toast } from "sonner";

export function useUploadAvatar() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: uploadAvatar,
        onSuccess: (profile) => {
            // Immediately update cache with new profile data
            queryClient.setQueryData(["profile"], profile);
            // Also invalidate to ensure fresh data on next fetch
            queryClient.invalidateQueries({queryKey: ["profile"], exact: true});
            return profile;
        },
        onError: (error) => {
            console.error(error);
            toast.error("Unable to load profile avatar")
        },
    });
}