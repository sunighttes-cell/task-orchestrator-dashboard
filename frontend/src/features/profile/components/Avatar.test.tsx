import { describe, it, expect } from "vitest";
import { renderWithProviders } from "@/test/test-utils";
import { screen } from "@testing-library/react";
import Avatar from "./Avatar";

describe("Avatar Component", () => {
  it("should render a default user icon when no src is provided", () => {
    const { container } = renderWithProviders(<Avatar />);
    const svg = container.querySelector("svg");
    expect(svg).toBeInTheDocument();
    expect(svg).toHaveClass("lucide-user");
  });

  it("should render an image when src is provided", () => {
    const testSrc = "https://example.com/avatar.jpg";
    renderWithProviders(<Avatar src={testSrc} alt="Test Avatar" />);
    const image = screen.getByRole("img", { name: "Test Avatar" });
    expect(image).toHaveAttribute("src", testSrc);
  });

  it("should render default alt text when alt is not provided", () => {
    const testSrc = "https://example.com/avatar.jpg";
    renderWithProviders(<Avatar src={testSrc} />);
    const image = screen.getByRole("img", { name: "Profile picture" });
    expect(image).toBeInTheDocument();
  });

  it("should apply correct size classes", () => {
    const { container } = renderWithProviders(
      <Avatar src="https://example.com/avatar.jpg" size="xl" />
    );
    const avatarContainer = container.querySelector("div");
    expect(avatarContainer).toHaveClass("h-24 w-24");
  });

  it("should apply custom className", () => {
    const { container } = renderWithProviders(
      <Avatar src="https://example.com/avatar.jpg" className="custom-class" />
    );
    const avatarContainer = container.querySelector("div");
    expect(avatarContainer).toHaveClass("custom-class");
  });

  it("should handle null src gracefully", () => {
    const { container } = renderWithProviders(<Avatar src={null} />);
    const svg = container.querySelector("svg");
    expect(svg).toBeInTheDocument();
    expect(svg).toHaveClass("lucide-user");
  });

  it("should have rounded-full class for circular shape", () => {
    const { container } = renderWithProviders(
      <Avatar src="https://example.com/avatar.jpg" />
    );
    const avatarContainer = container.querySelector("div");
    expect(avatarContainer).toHaveClass("rounded-full");
  });

  it("should show default user icon when src is undefined", () => {
    const { container } = renderWithProviders(<Avatar src={undefined} />);
    const svg = container.querySelector("svg");
    expect(svg).toBeInTheDocument();
  });

  it("should have correct image dimensions", () => {
    const { container } = renderWithProviders(
      <Avatar src="https://example.com/avatar.jpg" size="lg" />
    );
    const avatarContainer = container.querySelector("div");
    expect(avatarContainer).toHaveClass("h-16 w-16");
  });
});
