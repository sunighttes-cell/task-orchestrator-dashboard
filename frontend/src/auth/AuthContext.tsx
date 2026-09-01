import { createContext, useContext, useEffect, useState,} from "react";
import { subscribeToUnauthorized, subscribeToAuthUpdate,} from "@/auth/AuthEvents";
import { logoutUser, logoutAllUserSessions,} from "@/auth/api/AuthApi";
import { useQueryClient } from "@tanstack/react-query";
import { decodeUser } from "@/test/utils/jwt";
import type { CurrentUser, AuthContextType, AuthState,} from "@/types/auth";

const AuthContext = createContext<AuthContextType | null>(null);

//Safely decode the access token.
function decodeUserSafe( token: string): CurrentUser | null {
  try {
    return decodeUser(token);
  } catch {
    return null;
  }
}

//Load authentication state from sessionStorage.
function getStoredAuthState(): AuthState {
  const token = sessionStorage.getItem("token");
  const refreshToken = sessionStorage.getItem("refreshToken");
  const user = token ? decodeUserSafe(token) : null;

  //If the access token exists but cannot be decoded,
  //clear the complete authentication state.
  if (token && !user) {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("refreshToken");
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
  children,}: { children: React.ReactNode;}) {
  const queryClient = useQueryClient();
  const [auth, setAuth] = useState<AuthState>( 
    () => getStoredAuthState());

  //Update React authentication state.
  const setAuthenticatedState = (
    accessToken: string | null,
    refreshToken: string | null
  ): void => {

    if (!accessToken) {
      setAuth({
        token: null,
        refreshToken: null,
        user: null,
      });
      return;
    }

    const user = decodeUserSafe(accessToken);
    if (!user) {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("refreshToken");
      setAuth({
        token: null,
        refreshToken: null,
        user: null,
      });
      return;
    }

    setAuth({
      token: accessToken,
      refreshToken,
      user,
    });
  };

  //Login. Stores both access and refresh tokens.
  const login = (data: {
    accessToken: string;
    refreshToken: string;
  }): void => {

    const {
      accessToken,
      refreshToken,
    } = data;

    sessionStorage.setItem(
      "token",
      accessToken
    );

    sessionStorage.setItem(
      "refreshToken",
      refreshToken
    );

    setAuthenticatedState(
      accessToken,
      refreshToken
    );
  };

  //Registration automatically authenticates
  //the newly created user.
  const register = (data: {
    accessToken: string;
    refreshToken: string;
  }): void => {
    login(data);
  };

  //Clear local authentication state.
  //This is intentionally separate from the API logout call.
  const clearAuthentication = (): void => {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("refreshToken");
    setAuth({
      token: null,
      refreshToken: null,
      user: null,
    });
    //Remove cached authenticated data. Prevents the previous user's jobs/dashboard
    //from appearing after logout/login as another user.
    queryClient.clear();
  };

  //Logout the CURRENT refresh-token session.
  //Backend: POST /auth/logout
  const logout = async (): Promise<void> => {
    const refreshToken = sessionStorage.getItem("refreshToken");
    try {
      if (refreshToken) {
        await logoutUser(refreshToken);
      }
    } catch (error) {
      //Logout should still clear local credentials, even if backend request fails.
      //Examples: backend unavailable, token already expired, token already revoked
      console.warn(
        "Backend logout failed:",
        error
      );

    } finally {
      clearAuthentication();
    }
  };

  //Logout ALL sessions belonging to the user.
  //Backend: POST /auth/logout-all
  const logoutAll = async (): Promise<void> => {
    try {
      await logoutAllUserSessions();
    } catch (error) {
      console.warn(
        "Backend logout-all failed:",
        error
      );
    } finally {
      clearAuthentication();
    }
  };

  //Update authentication after token refresh.
  const updateAuthentication = (
    accessToken: string | null
  ): void => {
    const refreshToken = sessionStorage.getItem("refreshToken");
    setAuthenticatedState(
      accessToken,
      refreshToken
    );
  };

  //Authentication events.
  useEffect(() => {
    //Axios interceptor calls emitUnauthorized() when refresh authentication fails.
    const unsubscribeUnauthorized =
      subscribeToUnauthorized(
        clearAuthentication
      );

    //Token rotation calls emitAuthUpdate().
    const unsubscribeAuthUpdate =
      subscribeToAuthUpdate(
        (
          accessToken,
          refreshToken
        ) => {

          if (accessToken) {
            sessionStorage.setItem(
              "token",
              accessToken
            );
          }

          if (refreshToken) {
            sessionStorage.setItem(
              "refreshToken",
              refreshToken
            );
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
        logoutAll,
        updateAuthentication,
        isAuthenticated: !!auth.token,
      }}>
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