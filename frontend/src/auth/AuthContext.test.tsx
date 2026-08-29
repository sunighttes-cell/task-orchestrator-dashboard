import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { act, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext";
import { emitAuthUpdate, emitUnauthorized } from "./AuthEvents";

// A minimal JWT payload {"sub":"testuser","role":"USER"} signed with an
// empty signature — the decoder does not verify the signature.
const ACCESS_TOKEN =
  "eyJhbGciOiJIUzI1NiJ9." +
  "eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIn0." +
  "";

const ADMIN_TOKEN =
  "eyJhbGciOiJIUzI1NiJ9." +
  "eyJzdWIiOiJhZG1pbnVzZXIiLCJyb2xlIjoiQURNSU4ifQ." +
  "";

function wrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>{children}</AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe("AuthContext", () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it("starts unauthenticated when sessionStorage is empty", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.user).toBeNull();
  });

  it("login stores tokens and decodes user from JWT", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.auth.token).toBe(ACCESS_TOKEN);
    expect(result.current.auth.refreshToken).toBe("refresh-token-1");
    expect(result.current.auth.user).toEqual({
      username: "testuser",
      role: "USER",
    });
    expect(sessionStorage.getItem("token")).toBe(ACCESS_TOKEN);
    expect(sessionStorage.getItem("refreshToken")).toBe("refresh-token-1");
  });

  it("logout clears tokens and user state", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    act(() => {
      result.current.logout();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.auth.token).toBeNull();
    expect(result.current.auth.user).toBeNull();
    expect(sessionStorage.getItem("token")).toBeNull();
    expect(sessionStorage.getItem("refreshToken")).toBeNull();
  });

  it("hydrates from sessionStorage on mount", () => {
    sessionStorage.setItem("token", ADMIN_TOKEN);
    sessionStorage.setItem("refreshToken", "existing-refresh");

    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.auth.user).toEqual({
      username: "adminuser",
      role: "ADMIN",
    });
  });

  it("clears stale storage when the stored token cannot be decoded", () => {
    sessionStorage.setItem("token", "not-a-jwt");
    sessionStorage.setItem("refreshToken", "stale-refresh");

    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(sessionStorage.getItem("token")).toBeNull();
    expect(sessionStorage.getItem("refreshToken")).toBeNull();
  });

  it("reacts to emitUnauthorized by logging out", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
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
    expect(sessionStorage.getItem("token")).toBeNull();
  });

  it("reacts to emitAuthUpdate by swapping tokens without a full re-login", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: wrapper(),
    });

    act(() => {
      result.current.login({
        accessToken: ACCESS_TOKEN,
        refreshToken: "refresh-token-1",
      });
    });

    act(() => {
      emitAuthUpdate(ADMIN_TOKEN, "refresh-token-2");
    });

    expect(result.current.auth.token).toBe(ADMIN_TOKEN);
    expect(result.current.auth.refreshToken).toBe("refresh-token-2");
    expect(result.current.auth.user).toEqual({
      username: "adminuser",
      role: "ADMIN",
    });
  });
});
