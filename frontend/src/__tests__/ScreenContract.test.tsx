import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AppRouter } from "../AppRouter";

beforeEach(() => {
  vi.restoreAllMocks();
  vi.spyOn(globalThis, "fetch").mockResolvedValue({
    ok: false,
    json: async () => ({
      success: false,
      data: { code: "UNAUTHORIZED", message: "인증이 필요합니다." },
    }),
    status: 401,
  } as Response);
});

describe("management shell", () => {
  it("renders Korean login screen and uses admin seed defaults", async () => {
    render(
      <MemoryRouter initialEntries={["/login"]}>
        <AppRouter />
      </MemoryRouter>,
    );

    await waitFor(() =>
      expect(screen.getByText("교수업적평가 시스템")).toBeInTheDocument(),
    );
    expect(screen.getAllByDisplayValue("admin")).toHaveLength(2);
  });
});
