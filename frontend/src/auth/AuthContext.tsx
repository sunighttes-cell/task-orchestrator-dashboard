import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

import {
  subscribeToUnauthorized,
  subscribeToAuthUpdate,
} from "@/auth/AuthEvents";

import { useQueryClient } from "@tanstack/react-query";

import { decodeUser } from "@/test/utils/jwt";

import type {
  CurrentUser,
  AuthContextType,
  AuthState,
} from "@/types/auth";

const AuthContext =
  createContext<AuthContextType | null>(null);

function decodeUserSafe(
  token: string
): CurrentUser | null {

  try {
    return decodeUser(token);
  } catch {
    return null;
  }
}

function clearStoredAuthentication(): void {

  sessionStorage.removeItem("token");
  sessionStorage.removeItem("refreshToken");
}

function getStoredAuthState(): AuthState {

  const token =
    sessionStorage.getItem("token");

  const refreshToken =
    sessionStorage.getItem("refreshToken");

  const user =
    token
      ? decodeUserSafe(token)
      : null;

  if (token && !user) {

    clearStoredAuthentication();

    return {
      token: null,
      refreshToken: null,
      user: null,
    };
  }

  return {
    token,
    refreshToken,
    user,
  };
}

export function AuthProvider({
  children,
}: {
  children: React.ReactNode;
}) {

  const queryClient =
    useQueryClient();

  const [auth, setAuth] =
    useState<AuthState>(
      () => getStoredAuthState()
    );

  /**
   * Update authentication state.
   *
   * Access token and refresh token are treated as
   * one authentication pair.
   */
  const setAuthenticatedState = (
    accessToken: string | null,
    refreshToken: string | null
  ): void => {

    if (!accessToken || !refreshToken) {

      clearStoredAuthentication();

      setAuth({
        token: null,
        refreshToken: null,
        user: null,
      });

      return;
    }

    const user =
      decodeUserSafe(accessToken);

    if (!user) {

      clearStoredAuthentication();

      setAuth({
        token: null,
        refreshToken: null,
        user: null,
      });

      return;
    }

    /*
     * Keep storage synchronized with React state.
     */
    sessionStorage.setItem(
      "token",
      accessToken
    );

    sessionStorage.setItem(
      "refreshToken",
      refreshToken
    );

    setAuth({
      token: accessToken,
      refreshToken,
      user,
    });
  };

  /**
   * Register uses the same authentication flow as login.
   */
  const register = (data: {
    accessToken: string;
    refreshToken: string;
  }): void => {

    login(data);
  };

  /**
   * Store a newly authenticated session.
   */
  const login = (data: {
    accessToken: string;
    refreshToken: string;
  }): void => {

    setAuthenticatedState(
      data.accessToken,
      data.refreshToken
    );
  };

  /**
   * Clear local authentication state.
   */
  const logout = (): void => {

    clearStoredAuthentication();

    setAuth({
      token: null,
      refreshToken: null,
      user: null,
    });

    /*
     * Clear all React Query cached data.
     *
     * This is important so User A's data cannot remain
     * visible after User A logs out and User B logs in.
     */
    queryClient.clear();

    /*
     * If you have a Zustand store containing filters,
     * dashboard state, etc., reset that store here as well.
     *
     * Do NOT add a reset call until we know the actual
     * Zustand store API.
     */
  };

  /**
   * Update only the access token while retaining the
   * current refresh token.
   *
   * Kept for compatibility with existing consumers.
   */
  const updateAuthentication = (
    token: string | null
  ): void => {

    if (!token) {
      logout();
      return;
    }

    const refreshToken =
      auth.refreshToken ??
      sessionStorage.getItem("refreshToken");

    if (!refreshToken) {
      logout();
      return;
    }

    setAuthenticatedState(
      token,
      refreshToken
    );
  };

  /**
   * Subscribe to global authentication events.
   */
  useEffect(() => {

    const unsubscribeUnauthorized =
      subscribeToUnauthorized(logout);

    const unsubscribeAuthUpdate =
      subscribeToAuthUpdate(
        (
          accessToken,
          refreshToken
        ) => {

          if (!accessToken || !refreshToken) {
            logout();
            return;
          }

          setAuthenticatedState(
            accessToken,
            refreshToken
          );
        }
      );

    return () => {

      unsubscribeUnauthorized();
      unsubscribeAuthUpdate();
    };

  }, []);

  return (
    <AuthContext.Provider
      value={{
        auth,
        register,
        login,
        logout,
        updateAuthentication,
        isAuthenticated: !!auth.token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {

  const ctx =
    useContext(AuthContext);

  if (!ctx) {
    throw new Error(
      "useAuth must be used within AuthProvider"
    );
  }

  return ctx;
}