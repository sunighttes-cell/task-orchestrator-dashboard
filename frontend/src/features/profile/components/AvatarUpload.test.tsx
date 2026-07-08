import { describe, it, expect, beforeEach } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen, waitFor } from "@testing-library/react";
import AvatarUpload from "./AvatarUpload";
import { updateMockProfile } from "@/test/mock/handlers";
import { createMockProfile } from "@/test/factories/profile";

describe("AvatarUpload Component", () => {
  beforeEach(() => {
    updateMockProfile(createMockProfile({ profilePictureUrl: undefined }));
  });

  it("should render upload label", async () => {
    renderWithProviders(<AvatarUpload />);
    
    await waitFor(() => {
      expect(screen.getByText("Upload Profile Picture")).toBeInTheDocument();
    });
  });

  it("should display default avatar icon when no profile image", async () => {
    const { container } = renderWithProviders(<AvatarUpload />);
    
    await waitFor(() => {
      const svg = container.querySelector("svg");
      expect(svg).toBeInTheDocument();
      expect(svg).toHaveClass("lucide-user");
    });
  });

  it("should show existing profile image when available", async () => {
    updateMockProfile(
      createMockProfile({ profilePictureUrl: "https://example.com/avatar.jpg" })
    );
    renderWithProviders(<AvatarUpload />);

    await waitFor(() => {
      const image = screen.getByRole("img", { name: "Profile picture" });
      expect(image).toHaveAttribute("src", "https://example.com/avatar.jpg");
    });
  });

  it("should render with proper structure", async () => {
    const { container } = renderWithProviders(<AvatarUpload />);
    
    await waitFor(() => {
      const uploadLabel = screen.getByText("Upload Profile Picture");
      expect(uploadLabel).toBeInTheDocument();

      // Check avatar is present
      const avatarDiv = container.querySelector(
        "div.rounded-full.overflow-hidden.bg-gray-100"
      );
      expect(avatarDiv).toBeInTheDocument();
    });
  });

  it("should render avatar with xl size", async () => {
    const { container } = renderWithProviders(<AvatarUpload />);
    
    await waitFor(() => {
      const avatarDiv = container.querySelector(
        "div.rounded-full.overflow-hidden.bg-gray-100"
      );
      expect(avatarDiv).toHaveClass("h-24 w-24");
    });
  });

  it("should render the component without errors", async () => {
    const { container } = renderWithProviders(<AvatarUpload />);
    
    await waitFor(() => {
      expect(container).toBeInTheDocument();
      expect(container.querySelector("div.space-y-4")).toBeInTheDocument();
    });
  });
});
