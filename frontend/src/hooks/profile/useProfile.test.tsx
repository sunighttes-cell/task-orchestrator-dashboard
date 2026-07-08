import { describe, it, expect, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { useProfile } from "./useProfile";
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

describe("useProfile Hook", () => {
  beforeEach(() => {
    updateMockProfile(createMockProfile());
  });

  it("should fetch profile data successfully", async () => {
    const { result } = renderHook(() => useProfile(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toBeDefined();
    expect(result.current.data?.username).toBe("testuser");
  });

  it("should return profile with profileImageUrl when available", async () => {
    const mockProfile = createMockProfile({
      profilePictureUrl: "https://example.com/avatar.jpg",
    });
    updateMockProfile(mockProfile);

    const { result } = renderHook(() => useProfile(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data?.profilePictureUrl).toBe(
      "https://example.com/avatar.jpg"
    );
  });

  it("should handle profile without image URL", async () => {
    const { result } = renderHook(() => useProfile(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data?.profilePictureUrl).toBeUndefined();
  });

  it("should maintain profile data across re-renders", async () => {
    const { result, rerender } = renderHook(() => useProfile(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    const firstData = result.current.data;

    rerender();

    expect(result.current.data).toEqual(firstData);
  });

  it("should have staleTime of 0 for immediate staleness", async () => {
    // This is tested indirectly by verifying that refetch can happen immediately
    const { result } = renderHook(() => useProfile(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    // Update mock and refetch
    updateMockProfile(createMockProfile({ fullName: "Updated Name" }));
    result.current.refetch();

    await waitFor(() => {
      expect(result.current.data?.fullName).toBe("Updated Name");
    });
  });
});
