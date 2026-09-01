import {
  describe,
  it,
  expect,
  beforeEach,
  afterEach,
  vi,
} from "vitest";

import { act, renderHook } from "@testing-library/react";
import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";

import { AuthProvider, useAuth } from "./AuthContext";
import {
  emitAuthUpdate,
  emitUnauthorized,
} from "@/auth/AuthEvents";

import * as AuthApi from "@/auth/api/AuthApi";

// -----------------------------------------------------------------------------
// Mock AuthApi
// -----------------------------------------------------------------------------

vi.mock("@/auth/api/AuthApi", async () => {
  const actual = await vi.importActual<
    typeof import("@/auth/api/AuthApi")
  >("@/auth/api/AuthApi");

  return {
    ...actual,
    logoutUser: vi.fn(),
    logoutAllUserSessions: vi.fn(),
  };
});

// Test JWTs: decodeUser() only needs JWT payload.
const ACCESS_TOKEN =
  "eyJhbGciOiJIUzI1NiJ9." +
  "eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIn0." +
  "";

const ADMIN_TOKEN =
  "eyJhbGciOiJIUzI1NiJ9." +
  "eyJzdWIiOiJhZG1pbnVzZXIiLCJyb2xlIjoiQURNSU4ifQ." +
  "";

// Wrapper
function createWrapper(queryClient: QueryClient) {
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>{children}</AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

// Tests
describe("AuthContext", () => {
  beforeEach(() => {
    sessionStorage.clear();

    vi.mocked(AuthApi.logoutUser).mockReset();
    vi.mocked(AuthApi.logoutAllUserSessions).mockReset();
  });

  afterEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  // Initial state
  it("starts unauthenticated when sessionStorage is empty", () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.refreshToken).toBeNull();
    expect(result.current.auth.user).toBeNull();
  });

  // Login
  it("login stores access and refresh tokens and decodes the user", () => {
    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    expect(result.current.isAuthenticated).toBe(true);

    expect(result.current.auth.token).toBe(
      ACCESS_TOKEN,
    );

    expect(result.current.auth.refreshToken).toBe(
      "refresh-token-1",
    );

    expect(result.current.auth.user).toEqual({
      username: "testuser",
      role: "USER",
    });

    expect(sessionStorage.getItem("token")).toBe(
      ACCESS_TOKEN,
    );

    expect(sessionStorage.getItem("refreshToken")).toBe(
      "refresh-token-1",
    );
  });

  // Logout
  it("logout calls POST /auth/logout with the current refresh token", async () => {
    vi.mocked(AuthApi.logoutUser).mockResolvedValue(undefined);

    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(AuthApi.logoutUser).toHaveBeenCalledTimes(1);

    expect(AuthApi.logoutUser).toHaveBeenCalledWith(
      "refresh-token-1",
    );
  });

  it("logout clears authentication state and sessionStorage", async () => {
    vi.mocked(AuthApi.logoutUser).mockResolvedValue(undefined);

    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    expect(result.current.isAuthenticated).toBe(true);

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.isAuthenticated).toBe(false);

    expect(result.current.auth.token).toBeNull();

    expect(result.current.auth.refreshToken).toBeNull();

    expect(result.current.auth.user).toBeNull();

    expect(sessionStorage.getItem("token")).toBeNull();

    expect(
      sessionStorage.getItem("refreshToken"),
    ).toBeNull();
  });

  it("logout clears local authentication even when backend logout fails", async () => {
    vi.mocked(AuthApi.logoutUser).mockRejectedValue(
      new Error("Backend logout failed"),
    );

    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.isAuthenticated).toBe(false);

    expect(result.current.auth.token).toBeNull();

    expect(result.current.auth.refreshToken).toBeNull();

    expect(result.current.auth.user).toBeNull();

    expect(sessionStorage.getItem("token")).toBeNull();

    expect(
      sessionStorage.getItem("refreshToken"),
    ).toBeNull();
  });

  // Logout all
  it("logoutAll calls POST /auth/logout-all", async () => {
    vi.mocked(AuthApi.logoutAllUserSessions).mockResolvedValue(
      undefined,
    );

    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logoutAll();
    });

    expect(
      AuthApi.logoutAllUserSessions,
    ).toHaveBeenCalledTimes(1);
  });

  it("logoutAll clears all local authentication state", async () => {
    vi.mocked(AuthApi.logoutAllUserSessions).mockResolvedValue(
      undefined,
    );

    const queryClient = new QueryClient();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logoutAll();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.refreshToken).toBeNull();
    expect(result.current.auth.user).toBeNull();
    expect(sessionStorage.getItem("token")).toBeNull();
    expect(sessionStorage.getItem("refreshToken"),).toBeNull();
  });

  it("logoutAll clears local authentication even when backend request fails", async () => {
    vi.mocked(AuthApi.logoutAllUserSessions).mockRejectedValue(
      new Error("Backend logout-all failed"),
    );

    const queryClient = new QueryClient();
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logoutAll();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.refreshToken).toBeNull();
    expect(result.current.auth.user).toBeNull();
    expect(sessionStorage.getItem("token")).toBeNull();
    expect(sessionStorage.getItem("refreshToken"),
    ).toBeNull();
  });

  // Session hydration
  it("hydrates authentication state from sessionStorage", () => {
    sessionStorage.setItem(
      "token",
      ADMIN_TOKEN,
    );

    sessionStorage.setItem(
      "refreshToken",
      "existing-refresh",
    );

    const queryClient = new QueryClient();
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.auth.token).toBe(ADMIN_TOKEN,);
    expect(result.current.auth.refreshToken).toBe(
      "existing-refresh",
    );
    expect(result.current.auth.user).toEqual({
      username: "adminuser",
      role: "ADMIN",
    });
  });

  // Invalid/stale token
  it("clears stale storage when the stored access token cannot be decoded", () => {
    sessionStorage.setItem("token", "not-a-valid-jwt",);
    sessionStorage.setItem("refreshToken", "stale-refresh",);

    const queryClient = new QueryClient();
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(sessionStorage.getItem("token"),).toBeNull();
    expect(sessionStorage.getItem("refreshToken"),).toBeNull();
    expect(result.current.auth.user).toBeNull();
  });

  // Unauthorized event
  it("reacts to emitUnauthorized by clearing authentication", () => {
    const queryClient = new QueryClient();
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    expect(result.current.isAuthenticated).toBe(true);
    act(() => {
      emitUnauthorized();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.refreshToken,).toBeNull();
    expect(result.current.auth.user).toBeNull();
    expect(sessionStorage.getItem("token")).toBeNull();
    expect(sessionStorage.getItem("refreshToken"),).toBeNull();
  });

  // Auth update / refresh rotation
  it("reacts to emitAuthUpdate by replacing both access and refresh tokens", () => {
    const queryClient = new QueryClient();
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    act(() => {
      emitAuthUpdate(
        ADMIN_TOKEN,
        "refresh-token-2",
      );
    });

    expect(result.current.auth.token).toBe(ADMIN_TOKEN);
    expect(result.current.auth.refreshToken).toBe("refresh-token-2");
    expect(result.current.auth.user).toEqual({
      username: "adminuser",
      role: "ADMIN",
    });
    expect(sessionStorage.getItem("token")).toBe(ADMIN_TOKEN,);
    expect(sessionStorage.getItem("refreshToken"),).toBe("refresh-token-2");
  });

  // React Query cleanup
  it("clears the React Query cache when logging out", async () => {
    vi.mocked(AuthApi.logoutUser).mockResolvedValue(undefined);

    const queryClient = new QueryClient();

    queryClient.setQueryData(
      ["jobs"],
      {
        content: [
          {
            id: 1,
            status: "COMPLETED",
          },
        ],
      },
    );

    expect(
      queryClient.getQueryData(["jobs"]),
    ).toBeDefined();

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(queryClient),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(
      queryClient.getQueryData(["jobs"]),
    ).toBeUndefined();
  });
});
