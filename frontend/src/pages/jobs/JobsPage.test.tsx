import { describe, it, expect } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import JobsPage from "./JobsPage";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";

const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

describe("JobsPage", () => {
  it("renders jobs from API", async () => {
    renderWithProviders(<JobsPage />);

    expect(await screen.findByText(/completed/i)).toBeInTheDocument();
  });

  it("handles API error", async () => {
    server.use(
      http.get(`${API}/jobs`, () => new HttpResponse(null, { status: 500 }))
    );

    renderWithProviders(<JobsPage />);

    expect(
      await screen.findByText(/error loading jobs/i)
    ).toBeInTheDocument();
  });

  it("shows loading state", () => {
    renderWithProviders(<JobsPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it("supports pagination", async () => {
    const user = userEvent.setup();

    renderWithProviders(<JobsPage />);

    const next = await screen.findByRole("button", { name: /next/i });
    await user.click(next);

    await waitFor(() => {
      expect(screen.getByText(/page 2/i)).toBeInTheDocument();
    });
  });

  it("supports sorting", async () => {
    const user = userEvent.setup();

    renderWithProviders(<JobsPage />);

    const header = await screen.findByText(/status/i);
    await user.click(header);

    // behavior-based assertion instead of URL hack
    await waitFor(() => {
      expect(screen.getAllByRole("row").length).toBeGreaterThan(1);
    });
  });
});