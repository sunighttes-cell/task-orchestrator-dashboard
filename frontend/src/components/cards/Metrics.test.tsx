import { describe, it, expect } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen } from "@testing-library/react";
import MetricsGrid from "./MetricsGrid";
import { MetricCard } from "./MetricCard";

const metricsData = {
  totalJobs: 20,
  completedJobs: 16,
  runningJobs: 2,
  failedJobs: 2,
  successRate: 90.21,
  activeWorkers: 2,
  avgExecutionTime: 2.02,
};

describe("MetricCard", () => {
  it("renders title, value, and description", () => {
    renderWithProviders(
      <MetricCard
        title="Total Jobs"
        value={23}
        description="Total number of jobs processed"
      />
    );

    expect(screen.getByText("Total Jobs")).toBeInTheDocument();
    expect(screen.getByText("23")).toBeInTheDocument();
    expect(
      screen.getByText(/total number of jobs processed/i)
    ).toBeInTheDocument();
  });
});

describe("MetricsGrid", () => {
  it("renders all metric cards", () => {
    renderWithProviders(
      <MetricsGrid metrics={metricsData} isLoading={false} />
    );

    expect(screen.getByText(/total jobs/i)).toBeInTheDocument();
    expect(screen.getByText(/completed jobs/i)).toBeInTheDocument();
  });

  it("renders empty state", () => {
    renderWithProviders(
      <MetricsGrid metrics={null} isLoading={false} />
    );

    expect(screen.getByText(/no jobs found/i)).toBeInTheDocument();
    expect(screen.getByText(/try adjusting your search/i)).toBeInTheDocument();
  });

  it("renders loading state", () => {
    renderWithProviders(
      <MetricsGrid metrics={null} isLoading={true} />
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});