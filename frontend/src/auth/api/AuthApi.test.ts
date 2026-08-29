import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { http, HttpResponse } from "msw";
import { server } from "@/test/mock/server";
import { refreshAccessToken, registerUser } from "./AuthApi";
import * as AuthEvents from "@/auth/AuthEvents";

describe("AuthApi", () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  describe("refreshAccessToken", () => {
    it("throws when refresh token is missing", async () => {
      await expect(refreshAccessToken(null)).rejects.toThrow(
        /Missing refresh token/i,
      );
    });

    it("stores new access + refresh tokens in sessionStorage", async () => {
      server.use(
        http.post("/auth/refresh", () =>
          HttpResponse.json({
            accessToken: "new-access-token",
            refreshToken: "new-refresh-token",
          }),
        ),
      );

      const token = await refreshAccessToken("old-refresh-token");

      //expect(token).toBe("new-access-token");
      expect(sessionStorage.getItem("token")).toBe("new-access-token");
      expect(sessionStorage.getItem("refreshToken")).toBe(
        "new-refresh-token",
      );
    });

    it("emits an auth update event on successful refresh", async () => {
      const emitSpy = vi.spyOn(AuthEvents, "emitAuthUpdate");

      server.use(
        http.post("/auth/refresh", () =>
          HttpResponse.json({
            accessToken: "new-access-token",
            refreshToken: "new-refresh-token",
          }),
        ),
      );

      await refreshAccessToken("old-refresh-token");

      expect(emitSpy).toHaveBeenCalledWith(
        "new-access-token",
        "new-refresh-token",
      );
    });

    it("propagates a 401 from the refresh endpoint", async () => {
      server.use(
        http.post("/auth/refresh", () =>
          HttpResponse.json(
            { message: "Refresh token has been revoked" },
            { status: 401 },
          ),
        ),
      );

      await expect(
        refreshAccessToken("revoked-refresh-token"),
      ).rejects.toMatchObject({
        response: expect.objectContaining({ status: 401 }),
      });

      // Failed refresh must not leave stale tokens in storage.
      expect(sessionStorage.getItem("token")).toBeNull();
    });
  });

  describe("registerUser", () => {
    it("posts registration payload and returns tokens", async () => {
      server.use(
        http.post("/auth/register", async ({ request }) => {
          const body = (await request.json()) as Record<string, string>;

          expect(body.username).toBe("testuser");
          expect(body.email).toBe("test@example.com");

          return HttpResponse.json({
            accessToken: "new-access",
            refreshToken: "new-refresh",
          });
        }),
      );

      const result = await registerUser({
        username: "testuser",
        email: "test@example.com",
        fullName: "Test User",
        password: "Password123!",
      });

      expect(result).toEqual({
        accessToken: "new-access",
        refreshToken: "new-refresh",
      });
    });
  });
});
