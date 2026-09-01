import type { UpdatePasswordRequest, UpdateUserProfileRequest, UserProfile } from "@/types/profile";
import apiClient from "./client";

export async function getProfile(): Promise<UserProfile> {
  const res = await apiClient.get("/profile");
  return res.data;
}

export async function updateProfile(profileData: UpdateUserProfileRequest): Promise<UserProfile> {
  const res = await apiClient.put("/profile", profileData);
  return res.data;
}

export async function updatePassword(passwordData: UpdatePasswordRequest): Promise<UserProfile> {
  const res = await apiClient.put("/profile/password", passwordData);
  return res.data;
}

export async function uploadAvatar(file: File): Promise<UserProfile> {
    const formData = new FormData();
    formData.append("file", file);

    const { data } = await apiClient.post<UserProfile>(
        "/profile/avatar",
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
    return data;
}