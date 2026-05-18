import { describe, it, expect } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import JobsPage from "./JobsPage";
import { server } from "@/test/mock/server";
import { http, HttpResponse } from "msw";

describe("JobsPage", () => {
  it("renders jobs from API", async () => {
    renderWithProviders(<JobsPage />);

    expect(await screen.findByText(/completed/i)).toBeInTheDocument();
  });

  it("handles API error", async () => {
    server.use(
      http.get("/jobs", () => new HttpResponse(null, { status: 500 }))
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
    await waitFor(() => expect(next).not.toBeDisabled());
    await user.click(next);

    await waitFor(() => {
      const prev = screen.getByRole("button", { name: /prev/i });
      expect(prev).not.toBeDisabled();
    });
  });

  it("supports sorting", async () => {
    const user = userEvent.setup();

    renderWithProviders(<JobsPage />);

    const header = await screen.findByRole("columnheader", {
      name: /status/i,
    });

    await user.click(header);

    await waitFor(() => {
      const rows = screen.getAllByRole("row");
      expect(rows.length).toBeGreaterThan(1);
    });
  });
});