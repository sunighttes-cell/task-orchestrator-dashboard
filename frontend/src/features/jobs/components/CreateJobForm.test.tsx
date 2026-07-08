import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "@/auth/AuthContext";
import CreateJobForm from "./CreateJobForm";

function renderWithProviders(ui: React.ReactNode) {
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

describe("CreateJobForm", () => {
  it("should render create job button", () => {
    renderWithProviders(<CreateJobForm />);

    expect(screen.getByRole("button", { name: /Create Job/i })).toBeInTheDocument();
  });

  it("should open modal when button is clicked", async () => {
    const user = userEvent.setup();
    renderWithProviders(<CreateJobForm />);

    const createButton = screen.getByRole("button", { name: /Create Job/i });
    await user.click(createButton);

    // Modal should be displayed
    expect(screen.getByRole("button", { name: /close|cancel/i })).toBeInTheDocument();
  });

  it("should render create job button in clickable state", () => {
    renderWithProviders(<CreateJobForm />);

    const button = screen.getByRole("button", { name: /Create Job/i });
    expect(button).not.toBeDisabled();
  });
});
