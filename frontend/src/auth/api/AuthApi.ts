import axios from "axios";
import { emitAuthUpdate } from "@/auth/AuthEvents";
import type { RegisterUserRequest } from "@/types/auth";

export const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

//Response returned by:
//POST /auth/login
//POST /auth/register
//POST /auth/refresh
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

//Login
export async function loginUser(
  username: string,
  password: string
): Promise<AuthResponse> {
  const response = await authClient.post<AuthResponse>(
    "/auth/login",
    {
      username,
      password,
    }
  );
  return response.data;
}

//Refresh authentication. Backend rotates refresh token.
//BOTH returned values must replace existing values in sessionStorage.
export async function refreshAccessToken(
  refreshToken: string,
): Promise<AuthResponse> {
  if (!refreshToken) {
    throw new Error("Missing refresh token");
  }
  const response = await authClient.post<AuthResponse>(
    "/auth/refresh",
    { refreshToken },
  );

  const authResponse = response.data;
  sessionStorage.setItem(
    "token",
    authResponse.accessToken,
  );

  sessionStorage.setItem(
    "refreshToken",
    authResponse.refreshToken,
  );

  emitAuthUpdate(
    authResponse.accessToken,
    authResponse.refreshToken,
  );

  return authResponse;
}

//Register
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

//Logout the current refresh-token session. POST /auth/logout
//The backend validates refresh token belongs to the authenticated user.
export async function logoutUser(
  refreshToken: string | null
): Promise<void> {
  if (!refreshToken) {
    return;
  }
  await authClient.post(
    "/auth/logout",
    {
      refreshToken,
    }
  );
}

//Logout all sessions for authenticated user.
//POST /auth/logout-all invalidates every active refresh token.
export async function logoutAllUserSessions(): Promise<void> {
  await authClient.post("/auth/logout-all");
}
