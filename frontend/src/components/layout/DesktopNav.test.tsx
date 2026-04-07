import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { DesktopNav } from "./DesktopNav";

const logout = vi.fn();

vi.mock("@/contexts/AuthContext", () => ({
  useAuthContext: () => ({
    isAuthenticated: true,
    isAdmin: () => true,
    logout,
  }),
}));

vi.mock("@/hooks/use-desktop", () => ({
  useIsDesktop: () => true,
}));

vi.mock("./HeartsIndicator", () => ({
  HeartsIndicator: () => <div data-testid="hearts-indicator">Hearts</div>,
}));

describe("DesktopNav", () => {
  beforeEach(() => {
    logout.mockReset();
  });

  it("keeps desktop actions accessible when compact labels are hidden", () => {
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <DesktopNav />
      </MemoryRouter>
    );

    expect(screen.getByRole("navigation", { name: "Primary" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Profile" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Create" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Admin" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log Out" })).toBeInTheDocument();
    expect(screen.getByTestId("hearts-indicator")).toBeInTheDocument();
  });
});
