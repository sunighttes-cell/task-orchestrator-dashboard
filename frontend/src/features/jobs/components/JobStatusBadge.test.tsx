import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import JobStatusBadge from "./JobStatusBadge";

describe("JobStatusBadge", () => {
  it("should render status badge with provided status", () => {
    render(<JobStatusBadge status="COMPLETED" />);

    expect(screen.getByText(/Status:/i)).toBeInTheDocument();
    expect(screen.getByText("COMPLETED")).toBeInTheDocument();
  });

  it("should render status badge with RUNNING status", () => {
    render(<JobStatusBadge status="RUNNING" />);

    expect(screen.getByText(/Status:/i)).toBeInTheDocument();
    expect(screen.getByText("RUNNING")).toBeInTheDocument();
  });

  it("should render status badge with FAILED status", () => {
    render(<JobStatusBadge status="FAILED" />);

    expect(screen.getByText(/Status:/i)).toBeInTheDocument();
    expect(screen.getByText("FAILED")).toBeInTheDocument();
  });

  it("should handle null status gracefully", () => {
    render(<JobStatusBadge status={null} />);

    expect(screen.getByText(/Status:/i)).toBeInTheDocument();
  });

  it("should render with QUEUED status", () => {
    render(<JobStatusBadge status="QUEUED" />);

    expect(screen.getByText("QUEUED")).toBeInTheDocument();
  });
});
