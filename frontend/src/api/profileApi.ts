import type { UpdatePasswordRequest, UpdateUserProfileRequest, UserProfile } from "@/types/profile";
import apiClient from "./client";

export async function getProfile(): Promise<UserProfile> {
  console.log("Fetching user profile from API...");
  const res = await apiClient.get("/profile");
  console.log("getProfile response:", res.data);
  return res.data;
}

export async function updateProfile(profileData: UpdateUserProfileRequest): Promise<UserProfile> {
  console.log("Updating user profile with data:", profileData);
  const res = await apiClient.put("/profile", profileData);
  console.log("updateProfile response:", res.data);
  return res.data;
}

export async function updatePassword(passwordData: UpdatePasswordRequest): Promise<UserProfile> {
  console.log("Updating user password with data:", passwordData);
  const res = await apiClient.put("/profile/password", passwordData);
  console.log("updatePassword response:", res.data);
  return res.data;
}

export async function uploadAvatar(file: File): Promise<UserProfile> {
    console.log("Uploading profile picture:", file);
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
    console.log("uploadAvatar response:", data);
    return data;
}