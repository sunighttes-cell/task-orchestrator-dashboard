import { createContext, useContext, useEffect, useState } from "react";
import { subscribeToUnauthorized, subscribeToAuthUpdate } from "@/auth/AuthEvents";
import { useQueryClient } from "@tanstack/react-query";
import { decodeUser } from "@/test/utils/jwt";
import type { CurrentUser, AuthContextType, AuthState } from "@/types/auth";

const AuthContext = createContext<AuthContextType | null>(null);

function decodeUserSafe(token: string): CurrentUser | null {
  try {
    return decodeUser(token);
  } catch {
    return null;
  }
}

function getStoredAuthState(): AuthState {
  const token = sessionStorage.getItem("token");
  const refreshToken = sessionStorage.getItem("refreshToken");
  const user = token ? decodeUserSafe(token) : null;

  if (token && !user) {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("refreshToken");
    return { token: null, refreshToken: null, user: null };
  }

  return { token, refreshToken, user };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const queryClient = useQueryClient();
  const [auth, setAuth] = useState<AuthState>(() => getStoredAuthState());

  const setAuthenticatedState = (accessToken: string | null, refreshToken: string | null) => {
    if (!accessToken) {
      setAuth({ token: null, refreshToken: null, user: null });
      return;
    }

    const user = decodeUserSafe(accessToken);

    if (!user) {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("refreshToken");
      setAuth({ token: null, refreshToken: null, user: null });
      return;
    }

    setAuth({ token: accessToken, refreshToken, user });
  };

  const register = (data: { accessToken: string; refreshToken: string }) => {
    login(data);
  };

  const login = (data: { accessToken: string; refreshToken: string }) => {
    const { accessToken, refreshToken } = data;

    sessionStorage.setItem("token", accessToken);
    sessionStorage.setItem("refreshToken", refreshToken);

    setAuthenticatedState(accessToken, refreshToken);
  };

  const logout = () => {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("refreshToken");

    setAuth({ token: null, refreshToken: null, user: null });
    queryClient.clear();
  };

  const updateAuthentication = (token: string | null) => {
    const refreshToken = auth.refreshToken ?? sessionStorage.getItem("refreshToken");
    setAuthenticatedState(token, refreshToken);
  };

  useEffect(() => {
    const unsubscribeUnauthorized = subscribeToUnauthorized(logout);
    const unsubscribeAuthUpdate = subscribeToAuthUpdate((accessToken, refreshToken) => {
      if (accessToken) {
        sessionStorage.setItem("token", accessToken);
      }

      if (refreshToken) {
        sessionStorage.setItem("refreshToken", refreshToken);
      }

      setAuthenticatedState(accessToken, refreshToken);
    });

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

  // hydrate from storage
  // useEffect(() => {
  //   const stored = sessionStorage.getItem("token");
  //   if (stored) setToken(stored);
  // }, []);
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}