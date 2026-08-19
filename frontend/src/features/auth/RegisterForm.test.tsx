import {describe, it, expect, vi, beforeEach} from "vitest";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "@/test/test-utils";
import RegisterForm from "./RegisterForm";
import { useRegisterUser } from "@/hooks/profile/useRegisterUser";
import { usePasswordValidation } from "@/features/profile/hooks/usePasswordValidation";

vi.mock("@/hooks/profile/useRegisterUser", () => ({
  useRegisterUser: vi.fn(),
}));

vi.mock("@/features/profile/hooks/usePasswordValidation", () => ({
  usePasswordValidation: vi.fn(),
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

describe("RegisterForm", () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(useRegisterUser).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    } as unknown as ReturnType<typeof useRegisterUser>);

    vi.mocked(usePasswordValidation).mockReturnValue({
      valid: true,
      rules: [],
    } as unknown as ReturnType<typeof usePasswordValidation>);
  });

  it("renders the registration form", () => {
    renderWithProviders(<RegisterForm />);

    expect(
      screen.getByText(/Register as a new user/i),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/Username/i),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/email/i),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/Full Name/i),
    ).toBeInTheDocument();

    expect(
      screen.getByPlaceholderText(/Confirm Password/i),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", {
        name: /Register/i,
      }),
    ).toBeInTheDocument();
  });

  it("shows validation errors when fields are empty and submitted", async () => {
    const user = userEvent.setup();

    renderWithProviders(<RegisterForm />);

    await user.click(
      screen.getByRole("button", {
        name: /Register/i,
      }),
    );

    expect(
      await screen.findByText(
        /Username must be at least 5 characters/i,
      ),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /Invalid email address/i,
      ),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /Full name must be at least 3 characters/i,
      ),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /Password must be at least 8 characters/i,
      ),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /Please confirm password/i,
      ),
    ).toBeInTheDocument();
  });

  it("disables the submit button when password validation fails", () => {
    vi.mocked(usePasswordValidation).mockReturnValue({
      valid: false,
      rules: [],
    } as unknown as ReturnType<typeof usePasswordValidation>);

    renderWithProviders(<RegisterForm />);

    const submitBtn = screen.getByRole("button", {
      name: /Register/i,
    });

    expect(submitBtn).toBeDisabled();
  });

  it("navigates to login when cancel is clicked", async () => {
    const user = userEvent.setup();

    renderWithProviders(<RegisterForm />);

    await user.click(
      screen.getByRole("button", {
        name: /Cancel/i,
      }),
    );

    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });
});