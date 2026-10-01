import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuUsageManagementPage } from "./MenuUsageManagementPage";

const menu = (
  overrides: Partial<{
    menuId: string;
    menuName: string;
    parentMenuId: string | null;
    displayOrder: number;
    screenId: string | null;
    url: string | null;
    useYn: string;
    exposureStartAt: string | null;
    exposureEndAt: string | null;
  }> = {},
) => ({
  menuId: "MENU-MENU-USAGE-MANAGEMENT",
  menuName: "메뉴 사용 관리",
  parentMenuId: "MENU-MANAGEMENT",
  displayOrder: 3,
  screenId: "SCR-MENU-USAGE-MANAGEMENT",
  url: "/system/menus/usage",
  useYn: "Y",
  exposureStartAt: "2026-10-01T00:00:00Z",
  exposureEndAt: null,
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: status < 400, data, meta: {} }), {
      status,
    }),
  );

describe("SCR-MENU-USAGE-MANAGEMENT", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("queries, confirms an edit, and requeries the saved usage period", async () => {
    const savedMenu = menu({
      useYn: "N",
      exposureEndAt: "2026-12-31T23:59:59Z",
    });
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([menu()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response([savedMenu]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    render(<MenuUsageManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "수정" });

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(screen.getByLabelText("사용여부"), {
      target: { value: "N" },
    });
    fireEvent.change(screen.getByLabelText("노출 종료일시"), {
      target: { value: "2026-12-31T23:59" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText(
      "저장 후 메뉴 사용여부와 노출기간을 다시 조회했습니다.",
    );
    expect(window.confirm).toHaveBeenCalledWith(
      "메뉴 사용여부와 노출기간을 저장하시겠습니까?",
    );
    expect(fetchMock.mock.calls[1][0]).toBe("/api/menus");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/menus");
    expect(screen.getByText("N")).toBeInTheDocument();
  });

  it("keeps the edit local when confirmation is cancelled", async () => {
    const fetchMock = vi.fn().mockImplementation(() => response([menu()]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => false),
    );

    render(<MenuUsageManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "수정" });
    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).toBeInTheDocument();
  });

  it("shows an error after a failed save and a permission state after a forbidden query", async () => {
    const saveFailure = vi
      .fn()
      .mockImplementationOnce(() => response([menu()]))
      .mockImplementationOnce(() =>
        response({ error: { code: "INVALID" } }, 400),
      );
    vi.stubGlobal("fetch", saveFailure);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    const { unmount } = render(<MenuUsageManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "수정" });
    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    await screen.findByText("메뉴 사용여부와 노출기간을 저장하지 못했습니다.");
    unmount();

    vi.stubGlobal(
      "fetch",
      vi.fn(() => response({ error: { code: "FORBIDDEN" } }, 403)),
    );
    render(<MenuUsageManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "수정" }),
    ).not.toBeInTheDocument();
  });
});
