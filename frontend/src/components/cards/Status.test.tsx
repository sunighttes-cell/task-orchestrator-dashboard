import { describe, it, expect, vi } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen } from "@testing-library/react";
import StatusCard from "./StatusCard";
import StatusGrid from "./StatusGrid";
import { JobStatus } from "@/types/job";

const mockFn = vi.fn();

const job = {
  id: 1,
  name: "Job 1",
  description: "desc",
  status: JobStatus.QUEUED,
  retryCount: 0,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

describe("StatusCard", () => {
  it("renders job info", () => {
    renderWithProviders(<StatusCard job={job} onRetry={mockFn} />);

    expect(screen.getByText(/job 1/i)).toBeInTheDocument();
    expect(screen.getByText(/queued/i)).toBeInTheDocument();
  });
});

describe("StatusGrid", () => {
  it("renders jobs list", () => {
    renderWithProviders(
      <StatusGrid jobs={[job]} isLoading={false} onRetry={mockFn} />
    );

    expect(screen.getByText(/job 1/i)).toBeInTheDocument();
  });

  it("renders empty state", () => {
    renderWithProviders(
      <StatusGrid jobs={[]} isLoading={false} onRetry={mockFn} />
    );

    expect(screen.getByText(/no jobs found/i)).toBeInTheDocument();
  });

  it("renders loading state", () => {
    renderWithProviders(
      <StatusGrid jobs={[]} isLoading={true} onRetry={mockFn} />
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});