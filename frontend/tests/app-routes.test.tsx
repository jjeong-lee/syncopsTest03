import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { App } from "../src/app/App";

describe("App routes", () => {
  it("renders login screen while current user is unauthenticated", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        json: async () => ({
          success: false,
          error: { message: "unauthorized" },
          timestamp: new Date().toISOString(),
        }),
      }),
    );
    render(<App />);
    expect(
      await screen.findByText(/공통기능 1차 시스템 관리 로그인/),
    ).toBeInTheDocument();
  });
});
