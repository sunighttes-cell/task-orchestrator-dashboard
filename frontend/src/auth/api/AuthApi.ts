import axios from "axios";

import { emitAuthUpdate } from "@/auth/AuthEvents";

import type { RegisterUserRequest } from "@/types/auth";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

/**
 * Refresh the authentication session.
 * The backend uses refresh-token rotation:
 *
 * old refresh token
 *        ↓
 * /auth/refresh
 *        ↓
 * new access token
 * new refresh token
 *
 * The old refresh token must never continue to be used.
 */
export async function refreshAccessToken(
  refreshToken: string | null
): Promise<AuthResponse> {

  if (!refreshToken) {
    throw new Error("Missing refresh token");
  }

  const response = await authClient.post<AuthResponse>(
    "/auth/refresh",
    {
      refreshToken,
    }
  );

  const {
    accessToken,
    refreshToken: newRefreshToken,
  } = response.data;

  if (!accessToken) {
    throw new Error("Refresh response did not contain an access token");
  }

  if (!newRefreshToken) {
    throw new Error("Refresh response did not contain a refresh token");
  }

  //Refresh-token rotation means BOTH tokens are replaced.
  sessionStorage.setItem("token", accessToken);
  sessionStorage.setItem("refreshToken", newRefreshToken);

  emitAuthUpdate(accessToken, newRefreshToken);

  return {
    accessToken,
    refreshToken: newRefreshToken,
  };
}

//Register a new user.
export async function registerUser(
  registerUserRequest: RegisterUserRequest
): Promise<AuthResponse> {
  const response = await authClient.post<AuthResponse>(
    "/auth/register",
    {
      username: registerUserRequest.username,
      email: registerUserRequest.email,
      fullName: registerUserRequest.fullName,
      password: registerUserRequest.password,
    }
  );

  return response.data;
}