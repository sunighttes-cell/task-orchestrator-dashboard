import { describe, it, expect, vi } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SummaryCard from "./SummaryCard";
import SummaryGrid from "./SummaryGrid";
import { JobStatus } from "@/types/job";

const mockFn = vi.fn();

const summary = {
  count: 5,
  status: JobStatus.QUEUED,
};

describe("SummaryCard", () => {
  it("renders summary data", () => {
    renderWithProviders(
      <SummaryCard summary={summary} isSelected={false} onClick={mockFn} />
    );

    expect(screen.getByText(/queued/i)).toBeInTheDocument();
    expect(screen.getByText("5")).toBeInTheDocument();
  });

  it("calls onClick when clicked", async () => {
    const user = userEvent.setup();

    renderWithProviders(
      <SummaryCard summary={summary} isSelected={false} onClick={mockFn} />
    );

    await user.click(screen.getByText(/queued/i));

    expect(mockFn).toHaveBeenCalledWith(summary.status);
  });
});

describe("SummaryGrid", () => {
  it("renders summaries", () => {
    renderWithProviders(
      <SummaryGrid
        datasummary={[summary]}
        navigateBaseURL="/jobs"
        isLoading={false}
      />
    );

    expect(screen.getByText(/queued/i)).toBeInTheDocument();
  });

  it("renders empty state", () => {
    renderWithProviders(
      <SummaryGrid
        datasummary={[]}
        navigateBaseURL="/jobs"
        isLoading={false}
      />
    );

    expect(screen.getByText(/no jobs found/i)).toBeInTheDocument();
  });

  it("renders loading state", () => {
    renderWithProviders(
      <SummaryGrid
        datasummary={[]}
        navigateBaseURL="/jobs"
        isLoading={true}
      />
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});