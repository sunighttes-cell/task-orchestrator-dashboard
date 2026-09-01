//adjust based on changes in AuthContext

export interface CurrentUser {
    username: string;
    role: "USER" | "ADMIN";
}

export interface RegisterUserRequest {
    username: string,
    email: string,
    fullName: string,
    password: string
}

export interface JwtPayload {
    sub: string;
    type: string;
    iat: number;
    exp: number;
    role?: "USER" | "ADMIN";
}

export interface AuthState {
    token: string | null;
    refreshToken: string | null;
    user: CurrentUser | null;
}

export type AuthContextType = {
  auth: AuthState
  isAuthenticated: boolean;
  register: (data: { accessToken: string; refreshToken: string }) => void;
  login: (data: { accessToken: string; refreshToken: string }) => void;
  logout: () => void;
  logoutAll: () => Promise<void>;
  updateAuthentication: (token: string | null) => void;
};

