import axios from "axios";
import { emitAuthUpdate } from "@/auth/AuthEvents";
import type { RegisterUserRequest } from "@/types/auth";

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

export async function refreshAccessToken(
  refreshToken: string
): Promise<string> {
  const response = await authClient.post("/auth/refresh", {
    refreshToken,
  });

  const accessToken = response.data.accessToken;
  const newRefreshToken = response.data.refreshToken ?? null;

  if (newRefreshToken) {
    sessionStorage.setItem("refreshToken", newRefreshToken);
  }

  sessionStorage.setItem("token", accessToken);
  emitAuthUpdate(accessToken, newRefreshToken);

  return accessToken;
}

export async function registerUser( registerUserRequest: RegisterUserRequest): 
Promise<{ accessToken: string; refreshToken: string }> {

    console.log("UI: Registering user with request:", registerUserRequest);
    
    const response = await authClient.post("/auth/register", {
      username: registerUserRequest.username,
      email: registerUserRequest.email,
      fullName: registerUserRequest.fullName,
      password: registerUserRequest.password
    });

    return response.data;
  }