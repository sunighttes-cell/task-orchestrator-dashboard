import type { UserProfile } from "@/types/profile";

export function createMockProfile(overrides?: Partial<UserProfile>): UserProfile {
  return {
    id: 1,
    username: "testuser",
    role: "USER",
    fullName: "Test User",
    email: "test@example.com",
    profilePictureUrl: undefined,
    ...overrides,
  };
}

export function createMockProfileWithAvatar(
  overrides?: Partial<UserProfile>
): UserProfile {
  return createMockProfile({
    profilePictureUrl: "https://example.com/avatar.jpg",
    ...overrides,
  });
}
