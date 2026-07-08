import { describe, it, expect, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "@/test/test-utils";
import DashboardPage from "./DashboardPage";

const mockNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>(
    "react-router-dom"
  );
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("DashboardPage", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  it("renders summary cards", async () => {
    renderWithProviders(<DashboardPage />);

    expect(
      await screen.findByRole("button", { name: /completed/i })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /failed/i })
    ).toBeInTheDocument();
  });

  it("navigates when clicking a card", async () => {
    const user = userEvent.setup();

    renderWithProviders(<DashboardPage />);

    const failedCard = await screen.findByRole("button", {
      name: /failed/i,
    });

    await user.click(failedCard);

    expect(mockNavigate).toHaveBeenCalledWith(
      expect.stringContaining("status=FAILED")
    );
  });
});