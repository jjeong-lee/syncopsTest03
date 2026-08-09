import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuInformationManagementPage } from "./MenuInformationManagementPage";

const menu = (
  overrides: Partial<{
    menuId: string;
    menuName: string;
    parentMenuId: string | null;
    displayOrder: number;
    screenId: string | null;
    url: string | null;
    icon: string | null;
    businessCategory: string | null;
    description: string | null;
    useYn: string;
  }> = {},
) => ({
  menuId: "MENU-MENU-INFORMATION-MANAGEMENT",
  menuName: "메뉴 정보 관리",
  parentMenuId: "MENU-MANAGEMENT",
  displayOrder: 2,
  screenId: "SCR-MENU-INFORMATION-MANAGEMENT",
  url: "/system/menus/information",
  icon: null,
  businessCategory: "SYSTEM",
  description: null,
  useYn: "Y",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("MenuInformationManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("requeries and redisplays execution information and screen connection after editing a menu", async () => {
    const initialMenus = [menu()];
    const savedMenus = [
      menu({
        url: "/system/menus/information-updated",
        icon: "menu-file",
        businessCategory: "SYSTEM-MANAGEMENT",
        description: "메뉴 실행정보와 화면 연결을 관리합니다.",
      }),
    ];
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(initialMenus))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(savedMenus));
    vi.stubGlobal("fetch", fetchMock);

    render(<MenuInformationManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("SCR-MENU-INFORMATION-MANAGEMENT");

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(screen.getByLabelText("URL"), {
      target: { value: "/system/menus/information-updated" },
    });
    fireEvent.change(screen.getByLabelText("아이콘"), {
      target: { value: "menu-file" },
    });
    fireEvent.change(screen.getByLabelText("업무구분"), {
      target: { value: "SYSTEM-MANAGEMENT" },
    });
    fireEvent.change(screen.getByLabelText("설명"), {
      target: { value: "메뉴 실행정보와 화면 연결을 관리합니다." },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 메뉴 실행정보를 다시 조회했습니다.");
    expect(
      screen.getAllByText("/system/menus/information-updated"),
    ).not.toHaveLength(0);
    expect(screen.getAllByText("menu-file")).not.toHaveLength(0);
    expect(screen.getAllByText("SYSTEM-MANAGEMENT")).not.toHaveLength(0);
    expect(
      screen.getAllByText("메뉴 실행정보와 화면 연결을 관리합니다."),
    ).not.toHaveLength(0);
    expect(
      screen.getAllByText("SCR-MENU-INFORMATION-MANAGEMENT"),
    ).not.toHaveLength(0);
    expect(fetchMock.mock.calls[1][0]).toBe("/api/menus");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/menus");
  });

  it("shows permission state instead of management controls after a forbidden response", async () => {
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

    render(<MenuInformationManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "메뉴 등록" }),
    ).not.toBeInTheDocument();
  });
});
