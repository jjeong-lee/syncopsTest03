import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

const apiResponse = (data: unknown) =>
  new Response(JSON.stringify({ success: true, data, meta: {} }), {
    status: 200,
  });

describe("App menu authorization", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("renders only menus returned by the same current-user authorization response", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(
          apiResponse({
            menus: [
              {
                menuId: "MENU-MENU-PERMISSION-MANAGEMENT",
                menuName: "메뉴 권한 관리",
                route: "/system/roles-permissions/menu-permissions",
              },
            ],
          }),
        ),
    );

    render(<App />);

    await screen.findByRole("link", { name: "메뉴 권한 관리" });
    expect(
      screen.getByRole("link", { name: "메뉴 권한 관리" }),
    ).toHaveAttribute("href", "/system/roles-permissions/menu-permissions");
    expect(
      screen.queryByRole("link", { name: "사용자 관리" }),
    ).not.toBeInTheDocument();
  });

  it("shows a permission notice instead of a directly entered route when the current-user menu response excludes it", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(apiResponse({ menus: [] })),
    );

    render(<App />);

    await waitFor(() =>
      expect(
        screen.getByText("접근 권한이 있는 메뉴를 선택하세요."),
      ).toBeInTheDocument(),
    );
    expect(
      screen.queryByRole("button", { name: "조회" }),
    ).not.toBeInTheDocument();
  });

  it.each([
    ["/system/user-organization/users", "사용자 관리"],
    ["/system/user-organization/organizations", "조직 관리"],
    ["/system/roles-permissions/roles", "역할 관리"],
    ["/system/roles-permissions/user-roles", "사용자 역할 관리"],
    ["/system/roles-permissions/menu-permissions", "메뉴 권한 관리"],
    ["/system/menus/structure", "메뉴 구조 관리"],
    ["/system/menus/information", "메뉴 정보 관리"],
    ["/system/common-codes/groups", "코드그룹 관리"],
    [
      "/system/common-codes/detail-codes?groupId=CG-EMPLOYMENT-STATUS",
      "상세코드 관리",
    ],
  ])(
    "renders the authorized target screen for %s without a route error",
    async (route, heading) => {
      window.history.replaceState({}, "", route);
      vi.stubGlobal(
        "fetch",
        vi
          .fn()
          .mockResolvedValueOnce(apiResponse({ status: "UP" }))
          .mockResolvedValueOnce(
            apiResponse({
              menus: [
                {
                  menuId: "MENU-TARGET",
                  menuName: heading,
                  route: window.location.pathname,
                },
              ],
            }),
          ),
      );

      render(<App />);

      expect(
        await screen.findByRole("heading", { name: heading }),
      ).toBeInTheDocument();
      expect(
        screen.queryByText("접근 권한이 있는 메뉴를 선택하세요."),
      ).not.toBeInTheDocument();
    },
  );
});
