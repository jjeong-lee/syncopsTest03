import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuStructureManagementPage } from "./MenuStructureManagementPage";

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
  menuId: "MENU-USER-MANAGEMENT",
  menuName: "사용자 관리",
  parentMenuId: "MENU-USER-ORGANIZATION",
  displayOrder: 1,
  screenId: "SCR-USER-MANAGEMENT",
  url: "/system/user-organization/users",
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

describe("MenuStructureManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("requeries the menu tree after saving a selected menu parent and same-level display order", async () => {
    const initialMenus = [
      menu({
        menuId: "MENU-SYSTEM",
        menuName: "시스템 관리",
        parentMenuId: null,
        displayOrder: 1,
        screenId: null,
        url: null,
      }),
      menu({
        menuId: "MENU-USER-ORGANIZATION",
        menuName: "사용자·조직 관리",
        parentMenuId: "MENU-SYSTEM",
        screenId: null,
        url: null,
      }),
      menu(),
      menu({
        menuId: "MENU-ROLES-PERMISSIONS",
        menuName: "역할·권한 관리",
        parentMenuId: "MENU-SYSTEM",
        displayOrder: 2,
        screenId: null,
        url: null,
      }),
    ];
    const parentUpdatedMenus = initialMenus.map((item) =>
      item.menuId === "MENU-USER-MANAGEMENT"
        ? { ...item, parentMenuId: "MENU-ROLES-PERMISSIONS" }
        : item,
    );
    const orderUpdatedMenus = parentUpdatedMenus.map((item) =>
      item.menuId === "MENU-USER-MANAGEMENT"
        ? { ...item, displayOrder: 0 }
        : item,
    );
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(initialMenus))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(parentUpdatedMenus))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(orderUpdatedMenus));
    vi.stubGlobal("fetch", fetchMock);

    render(<MenuStructureManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "사용자 관리" });

    fireEvent.click(screen.getByRole("button", { name: "사용자 관리" }));
    fireEvent.change(screen.getByLabelText("부모메뉴"), {
      target: { value: "MENU-ROLES-PERMISSIONS" },
    });
    fireEvent.change(screen.getByLabelText("사유"), {
      target: { value: "메뉴 구조 조정" },
    });
    fireEvent.click(screen.getByRole("button", { name: "부모 변경 저장" }));

    await screen.findByText(
      "저장 후 메뉴 계층과 표시순서를 다시 조회했습니다.",
    );
    expect(screen.getByText("역할·권한 관리 아래")).toBeInTheDocument();
    expect(fetchMock.mock.calls[1][0]).toBe("/api/menus");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });

    fireEvent.change(screen.getByLabelText("표시순서"), {
      target: { value: "0" },
    });
    fireEvent.click(screen.getByRole("button", { name: "순서 저장" }));

    await screen.findByText("표시순서: 0");
    expect(fetchMock.mock.calls[3][0]).toBe("/api/menus/order");
    expect(fetchMock.mock.calls[3][1]).toMatchObject({ method: "PUT" });
    expect(fetchMock.mock.calls[4][0]).toBe("/api/menus");
  });

  it("shows a permission state instead of the menu tree after a forbidden response", async () => {
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

    render(<MenuStructureManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "부모 변경 저장" }),
    ).not.toBeInTheDocument();
  });
});
