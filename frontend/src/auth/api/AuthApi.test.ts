import {
  describe,
  it,
  expect,
  beforeEach,
  afterEach,
  vi,
} from "vitest";

import {
  http,
  HttpResponse,
} from "msw";

import { server } from "@/test/mock/server";

import {
  loginUser,
  refreshAccessToken,
  registerUser,
  logoutUser,
  logoutAllUserSessions,
} from "./AuthApi";

import * as AuthEvents from "@/auth/AuthEvents";

describe("AuthApi", () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  // ===========================================================================
  // LOGIN
  // ===========================================================================

  describe("loginUser", () => {
    it("posts username and password and returns both tokens", async () => {
      server.use(
        http.post(
          "/auth/login",
          async ({ request }) => {
            const body =
              (await request.json()) as Record<
                string,
                string
              >;

            expect(body.username).toBe(
              "testuser",
            );

            expect(body.password).toBe(
              "Password123!",
            );

            return HttpResponse.json({
              accessToken:
                "access-token",
              refreshToken:
                "refresh-token",
            });
          },
        ),
      );

      const result = await loginUser(
        "testuser",
        "Password123!",
      );

      expect(result).toEqual({
        accessToken:
          "access-token",
        refreshToken:
          "refresh-token",
      });
    });

    it("propagates a 401 from login", async () => {
      server.use(
        http.post(
          "/auth/login",
          () =>
            HttpResponse.json(
              {
                message:
                  "Invalid username or password",
              },
              {
                status: 401,
              },
            ),
        ),
      );

      await expect(
        loginUser(
          "testuser",
          "wrong-password",
        ),
      ).rejects.toMatchObject({
        response: expect.objectContaining({
          status: 401,
        }),
      });
    });
  });

  // ===========================================================================
  // REFRESH TOKEN ROTATION
  // ===========================================================================

  describe("refreshAccessToken", () => {
    it("throws when refresh token is missing", async () => {
      await expect(
        refreshAccessToken(null),
      ).rejects.toThrow(
        /Missing refresh token/i,
      );
    });

    it("sends the current refresh token to the backend", async () => {
      server.use(
        http.post(
          "/auth/refresh",
          async ({ request }) => {
            const body =
              (await request.json()) as {
                refreshToken: string;
              };

            expect(
              body.refreshToken,
            ).toBe("old-refresh-token");

            return HttpResponse.json({
              accessToken:
                "new-access-token",
              refreshToken:
                "new-refresh-token",
            });
          },
        ),
      );

      await refreshAccessToken(
        "old-refresh-token",
      );
    });

    it("stores both the new access token and rotated refresh token", async () => {
      server.use(
        http.post(
          "/auth/refresh",
          () =>
            HttpResponse.json({
              accessToken:
                "new-access-token",
              refreshToken:
                "new-refresh-token",
            }),
        ),
      );

      const token =
        await refreshAccessToken(
          "old-refresh-token",
        );

      expect(token).toStrictEqual(
        {
          "accessToken": "new-access-token",
          "refreshToken": "new-refresh-token",
        }
      );

      expect(
        sessionStorage.getItem("token"),
      ).toBe("new-access-token");

      expect(
        sessionStorage.getItem(
          "refreshToken",
        ),
      ).toBe("new-refresh-token");
    });

    it("replaces the old refresh token after rotation", async () => {
      sessionStorage.setItem(
        "token",
        "old-access-token",
      );

      sessionStorage.setItem(
        "refreshToken",
        "old-refresh-token",
      );

      server.use(
        http.post(
          "/auth/refresh",
          () =>
            HttpResponse.json({
              accessToken:
                "new-access-token",
              refreshToken:
                "new-refresh-token",
            }),
        ),
      );

      await refreshAccessToken(
        "old-refresh-token",
      );

      expect(
        sessionStorage.getItem(
          "refreshToken",
        ),
      ).toBe("new-refresh-token");

      expect(
        sessionStorage.getItem(
          "refreshToken",
        ),
      ).not.toBe(
        "old-refresh-token",
      );
    });

    it("emits an auth update containing both rotated tokens", async () => {
      const emitSpy =
        vi.spyOn(
          AuthEvents,
          "emitAuthUpdate",
        );

      server.use(
        http.post(
          "/auth/refresh",
          () =>
            HttpResponse.json({
              accessToken:
                "new-access-token",
              refreshToken:
                "new-refresh-token",
            }),
        ),
      );

      await refreshAccessToken(
        "old-refresh-token",
      );

      expect(
        emitSpy,
      ).toHaveBeenCalledWith(
        "new-access-token",
        "new-refresh-token",
      );
    });

    it("propagates a 401 when the refresh token has been revoked", async () => {
      server.use(
        http.post(
          "/auth/refresh",
          () =>
            HttpResponse.json(
              {
                code: "UNAUTHORIZED",
                message:
                  "Refresh token has been revoked",
              },
              {
                status: 401,
              },
            ),
        ),
      );

      await expect(
        refreshAccessToken(
          "revoked-refresh-token",
        ),
      ).rejects.toMatchObject({
        response: expect.objectContaining({
          status: 401,
        }),
      });
    });

    it("does not replace the existing tokens when refresh fails", async () => {
      sessionStorage.setItem(
        "token",
        "existing-access-token",
      );

      sessionStorage.setItem(
        "refreshToken",
        "existing-refresh-token",
      );

      server.use(
        http.post(
          "/auth/refresh",
          () =>
            HttpResponse.json(
              {
                code: "UNAUTHORIZED",
                message:
                  "Refresh token has been revoked",
              },
              {
                status: 401,
              },
            ),
        ),
      );

      await expect(
        refreshAccessToken(
          "existing-refresh-token",
        ),
      ).rejects.toMatchObject({
        response: expect.objectContaining({
          status: 401,
        }),
      });

      expect(
        sessionStorage.getItem("token"),
      ).toBe(
        "existing-access-token",
      );

      expect(
        sessionStorage.getItem(
          "refreshToken",
        ),
      ).toBe(
        "existing-refresh-token",
      );
    });
  });

  // ===========================================================================
  // REGISTER
  // ===========================================================================

  describe("registerUser", () => {
    it("posts registration payload and returns tokens", async () => {
      server.use(
        http.post(
          "/auth/register",
          async ({ request }) => {
            const body =
              (await request.json()) as Record<
                string,
                string
              >;

            expect(body.username).toBe(
              "testuser",
            );

            expect(body.email).toBe(
              "test@example.com",
            );

            expect(body.fullName).toBe(
              "Test User",
            );

            expect(body.password).toBe(
              "Password123!",
            );

            return HttpResponse.json({
              accessToken:
                "new-access",
              refreshToken:
                "new-refresh",
            });
          },
        ),
      );

      const result =
        await registerUser({
          username: "testuser",
          email: "test@example.com",
          fullName: "Test User",
          password: "Password123!",
        });

      expect(result).toEqual({
        accessToken:
          "new-access",
        refreshToken:
          "new-refresh",
      });
    });
  });

  // ===========================================================================
  // LOGOUT
  // ===========================================================================

  describe("logoutUser", () => {
    it("posts the refresh token to /auth/logout", async () => {
      server.use(
        http.post(
          "/auth/logout",
          async ({ request }) => {
            const body =
              (await request.json()) as {
                refreshToken: string;
              };

            expect(
              body.refreshToken,
            ).toBe(
              "refresh-token-1",
            );

            return new HttpResponse(
              null,
              {
                status: 204,
              },
            );
          },
        ),
      );

      await expect(
        logoutUser(
          "refresh-token-1",
        ),
      ).resolves.toBeUndefined();
    });

    it("propagates a 401 from logout", async () => {
      server.use(
        http.post(
          "/auth/logout",
          () =>
            HttpResponse.json(
              {
                code: "UNAUTHORIZED",
                message:
                  "Invalid refresh token",
              },
              {
                status: 401,
              },
            ),
        ),
      );

      await expect(
        logoutUser(
          "invalid-refresh-token",
        ),
      ).rejects.toMatchObject({
        response: expect.objectContaining({
          status: 401,
        }),
      });
    });
  });

  // ===========================================================================
  // LOGOUT ALL
  // ===========================================================================

  describe("logoutAllUserSessions", () => {
    it("posts to /auth/logout-all", async () => {
      server.use(
        http.post(
          "/auth/logout-all",
          () =>
            new HttpResponse(
              null,
              {
                status: 204,
              },
            ),
        ),
      );

      await expect(
        logoutAllUserSessions(),
      ).resolves.toBeUndefined();
    });

    it("propagates a 401 from logout-all", async () => {
      server.use(
        http.post(
          "/auth/logout-all",
          () =>
            HttpResponse.json(
              {
                code: "UNAUTHORIZED",
                message:
                  "User not found",
              },
              {
                status: 401,
              },
            ),
        ),
      );

      await expect(
        logoutAllUserSessions(),
      ).rejects.toMatchObject({
        response: expect.objectContaining({
          status: 401,
        }),
      });
    });
  });
});
