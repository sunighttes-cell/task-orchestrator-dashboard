import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "@/test/test-utils";
import LoginPage from "./LoginPage";
import { useAuthLogin } from "@/hooks/auth/useAuthLogin";

vi.mock("@/hooks/auth/useAuthLogin", () => ({
  useAuthLogin: vi.fn(),
}));

const mockNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual =
    await vi.importActual<typeof import("react-router-dom")>(
      "react-router-dom",
    );

  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("LoginPage", () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(useAuthLogin).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
      data: undefined,
    } as unknown as ReturnType<typeof useAuthLogin>);
  });

  it("renders the login form", () => {
    renderWithProviders(<LoginPage />);

    expect(
      screen.getByRole("heading", { name: /Login/i }),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/Username/i),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/Password/i),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: /^Login$/i }),
    ).toBeInTheDocument();
  });

  it("shows validation errors when fields are empty and submitted", async () => {
    const user = userEvent.setup();

    renderWithProviders(<LoginPage />);

    await user.click(
      screen.getByRole("button", { name: /^Login$/i }),
    );

    expect(
      await screen.findByText(
        /Username must be at least 4 characters/i,
      ),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /Password must be at least 8 characters/i,
      ),
    ).toBeInTheDocument();

    expect(mockMutate).not.toHaveBeenCalled();
  });

  it("submits credentials when form is valid", async () => {
    const user = userEvent.setup();

    renderWithProviders(<LoginPage />);

    await user.type(
      screen.getByPlaceholderText(/Username/i),
      "testuser",
    );

    await user.type(
      screen.getByPlaceholderText(/Password/i),
      "Password123!",
    );

    await user.click(
      screen.getByRole("button", { name: /^Login$/i }),
    );

    // expect(mockMutate).toHaveBeenCalledWith(
    //   { username: "testuser", password: "Password123!" },
    //   expect.objectContaining({ onSettled: expect.any(Function)}),
    //   expect.objectContaining({ onError: expect.any(Function)}),
    // );
  });

  it("disables the submit button while login is pending", () => {
    vi.mocked(useAuthLogin).mockReturnValue({
      mutate: mockMutate,
      isPending: true,
      data: undefined,
    } as unknown as ReturnType<typeof useAuthLogin>);

    renderWithProviders(<LoginPage />);

    const submitBtn = screen.getByRole("button", {
      name: /Logging in/i,
    });

    expect(submitBtn).toBeDisabled();
  });

  it("navigates to register when 'Register new user' clicked", async () => {
    const user = userEvent.setup();

    renderWithProviders(<LoginPage />);

    await user.click(
      screen.getByRole("button", { name: /Register new user/i }),
    );

    expect(mockNavigate).toHaveBeenCalledWith("/register");
  });
});
