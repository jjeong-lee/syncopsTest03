import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AppRouter } from "../src/app/AppRouter";

const session = {
  userId: "admin",
  loginId: "admin",
  displayName: "관리자",
  roleCodes: ["R09"],
  menus: [
    {
      menuId: "M1",
      menuName: "사용자 관리",
      routePath: "/system/users",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M2",
      menuName: "조직 관리",
      routePath: "/system/organizations",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M3",
      menuName: "역할 관리",
      routePath: "/system/roles",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M4",
      menuName: "사용자 역할 관리",
      routePath: "/system/user-roles",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M5",
      menuName: "메뉴 권한 관리",
      routePath: "/system/menu-permissions",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M6",
      menuName: "메뉴 구조 관리",
      routePath: "/system/menu-structure",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M7",
      menuName: "메뉴 정보 관리",
      routePath: "/system/menu-info",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M8",
      menuName: "코드그룹 관리",
      routePath: "/system/code-groups",
      permissionLevel: "WRITE",
    },
    {
      menuId: "M9",
      menuName: "상세코드 관리",
      routePath: "/system/code-details",
      permissionLevel: "WRITE",
    },
  ],
};

describe("AppRouter", () => {
  it("renders protected routes with React Router and active menu", async () => {
    window.history.pushState({}, "", "/system/roles");
    vi.stubGlobal(
      "fetch",
      vi.fn(async (path: string) => {
        if (path === "/api/auth/me")
          return {
            ok: true,
            json: async () => ({ success: true, data: session }),
          };
        if (path.startsWith("/api/roles"))
          return {
            ok: true,
            json: async () => ({
              success: true,
              data: { items: [], page: 0, size: 20, totalElements: 0 },
            }),
          };
        return {
          ok: true,
          json: async () => ({ success: true, data: { items: [] } }),
        };
      }),
    );
    render(<AppRouter />);
    expect(
      await screen.findByRole("heading", { name: "역할 관리" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "역할 관리" })).toHaveAttribute(
      "href",
      "/system/roles",
    );
  });
});
