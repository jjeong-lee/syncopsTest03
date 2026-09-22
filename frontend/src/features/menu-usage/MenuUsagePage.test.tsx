import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuUsagePage } from "./MenuUsagePage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

const setting = {
  menuId: "MENU-MENU-INFORMATION-MANAGEMENT",
  useYn: "Y",
  exposureStartAt: "2026-09-22T09:00:00Z",
  exposureEndAt: null,
};

const menu = {
  menuId: setting.menuId,
  menuName: "메뉴 정보 관리",
  parentMenuId: "MENU-MANAGEMENT",
  displayOrder: 2,
  screenId: "SCR-MENU-INFORMATION-MANAGEMENT",
  url: "/system/menus/information",
  icon: null,
  businessCategory: "SYSTEM",
  description: null,
  useYn: "Y",
};

describe("MenuUsagePage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("saves a selected menu usage setting only after confirmation and requeries the last criteria", async () => {
    const savedSetting = { ...setting, useYn: "N" };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([menu]))
      .mockImplementationOnce(() => response([setting]))
      .mockImplementationOnce(() => response(savedSetting))
      .mockImplementationOnce(() => response([menu]))
      .mockImplementationOnce(() => response([savedSetting]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    render(<MenuUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("메뉴 정보 관리");

    fireEvent.click(screen.getByRole("button", { name: "변경" }));
    fireEvent.change(screen.getByTestId("menu-usage-edit-use-yn-select"), {
      target: { value: "N" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText(
      "저장 후 메뉴 사용여부와 노출기간을 다시 조회했습니다.",
    );
    expect(fetchMock.mock.calls[2][0]).toBe("/api/system/menus/usage");
    expect(fetchMock.mock.calls[2][1]).toMatchObject({ method: "POST" });
    expect(JSON.parse(fetchMock.mock.calls[2][1].body)).toMatchObject({
      menuId: setting.menuId,
      useYn: "N",
    });
    expect(fetchMock.mock.calls[4][0]).toContain("size=20");
    expect(screen.getAllByText("중지")).not.toHaveLength(0);
  });

  it("shows the permission state after a forbidden usage query", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "FORBIDDEN" },
            meta: {},
          }),
          { status: 403 },
        ),
      ),
    );

    render(<MenuUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText(
      "권한이 없어 메뉴 사용 설정을 조회하거나 변경할 수 없습니다.",
    );
    expect(
      screen.queryByRole("button", { name: "변경" }),
    ).not.toBeInTheDocument();
  });

  it("shows a loading notice while a usage query is pending", () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(() => new Promise<Response>(() => undefined)),
    );

    render(<MenuUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    expect(
      screen.getByText("메뉴 사용 설정을 조회하고 있습니다."),
    ).toBeInTheDocument();
  });

  it("shows the empty state when the current criteria have no settings", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockImplementationOnce(() => response([]))
        .mockImplementationOnce(() => response([])),
    );

    render(<MenuUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("현재 조건에 맞는 메뉴 사용 설정이 없습니다.");
  });

  it("shows a retry action after a non-permission query failure", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "INTERNAL_ERROR" },
            meta: {},
          }),
          { status: 500 },
        ),
      ),
    );

    render(<MenuUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("메뉴 사용 설정을 조회하지 못했습니다.");
    expect(
      screen.getByRole("button", { name: "다시 시도" }),
    ).toBeInTheDocument();
  });
});
