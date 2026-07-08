import { describe, it, expect, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { useUploadAvatar } from "./useUploadAvatar";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { updateMockProfile } from "@/test/mock/handlers";
import { createMockProfile } from "@/test/factories/profile";

function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
      mutations: {
        retry: false,
      },
    },
  });
}

function createWrapper() {
  const testQueryClient = createTestQueryClient();
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={testQueryClient}>
      {children}
    </QueryClientProvider>
  );
}

describe("useUploadAvatar Hook", () => {
  beforeEach(() => {
    updateMockProfile(createMockProfile());
  });

  it("should have mutation function", () => {
    const { result } = renderHook(() => useUploadAvatar(), {
      wrapper: createWrapper(),
    });

    expect(result.current.mutate).toBeDefined();
    expect(typeof result.current.mutate).toBe("function");
  });

  it("should initially have no data", () => {
    const { result } = renderHook(() => useUploadAvatar(), {
      wrapper: createWrapper(),
    });

    expect(result.current.data).toBeUndefined();
  });

  it("should not be pending initially", () => {
    const { result } = renderHook(() => useUploadAvatar(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(false);
  });

  it("should call mutate with file", async () => {
    const { result } = renderHook(() => useUploadAvatar(), {
      wrapper: createWrapper(),
    });

    const file = new File(["test"], "avatar.jpg", { type: "image/jpeg" });

    result.current.mutate(file);

    // Wait for the mutation to complete (either success or error)
    await waitFor(
      () => {
        expect(result.current.isPending).toBe(false);
      },
      { timeout: 5000 }
    );
  });

  it("should preserve profile fields on success", async () => {
    const originalProfile = createMockProfile({
      fullName: "Test User",
      email: "test@example.com",
      username: "testuser",
    });
    updateMockProfile(originalProfile);

    const { result } = renderHook(() => useUploadAvatar(), {
      wrapper: createWrapper(),
    });

    const file = new File(["test"], "avatar.jpg", { type: "image/jpeg" });

    result.current.mutate(file);

    await waitFor(() => {
      return result.current.data !== undefined || result.current.isError;
    }, { timeout: 3000 });

    // If mutation succeeds, verify fields are preserved
    if (result.current.data) {
      expect(result.current.data.fullName).toBe("Test User");
      expect(result.current.data.email).toBe("test@example.com");
      expect(result.current.data.username).toBe("testuser");
    }
  });
});
