import { describe, it, expect, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import userEvent from "@testing-library/user-event";
import ProfileAvatar from "./ProfileAvatar";
import { updateMockProfile } from "@/test/mock/handlers";
import { createMockProfile } from "@/test/factories/profile";
import { AuthProvider } from "@/auth/AuthContext";

function renderWithAuth(ui: React.ReactNode) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>{ui}</AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe("ProfileAvatar Component", () => {
  beforeEach(() => {
    updateMockProfile(
      createMockProfile({
        profilePictureUrl: "https://example.com/avatar.jpg",
      })
    );
  });

  it("should render avatar button", async () => {
    renderWithAuth(<ProfileAvatar />);

    await waitFor(() => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      expect(button).toBeInTheDocument();
    });
  });

  it("should display user profile image", async () => {
    renderWithAuth(<ProfileAvatar />);

    await waitFor(() => {
      const image = screen.getByRole("img", {
        name: "Test User",
      });
      expect(image).toHaveAttribute("src", "https://example.com/avatar.jpg");
    });
  });

  it("should show profile menu when avatar is clicked", async () => {
    const user = userEvent.setup();
    renderWithAuth(<ProfileAvatar />);

    await waitFor(() => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      expect(button).toBeInTheDocument();
    });

    const button = screen.getByRole("button", {
      name: "Open profile menu",
    });
    await user.click(button);

    expect(screen.getByText("Test User")).toBeInTheDocument();
    expect(screen.getByText("@testuser")).toBeInTheDocument();
  });

  it("should show view profile link", async () => {
    const user = userEvent.setup();
    renderWithAuth(<ProfileAvatar />);

    await waitFor(() => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      expect(button).toBeInTheDocument();
    });

    const button = screen.getByRole("button", {
      name: "Open profile menu",
    });
    await user.click(button);

    expect(screen.getByText("View Profile")).toBeInTheDocument();
  });

  it("should show logout button", async () => {
    const user = userEvent.setup();
    renderWithAuth(<ProfileAvatar />);

    await waitFor(() => {
      const button = screen.getByRole("button", {
        name: "Open profile menu",
      });
      expect(button).toBeInTheDocument();
    });

    const button = screen.getByRole("button", {
      name: "Open profile menu",
    });
    await user.click(button);

    const logoutButton = screen.getByText("Logout");
    expect(logoutButton).toBeInTheDocument();
  });
});
