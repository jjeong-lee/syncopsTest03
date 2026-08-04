import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "../src/App";

const menus = [
  ["M-USERS", "사용자 관리", "CMN-FR-001", "/system/users"],
  ["M-ORGS", "조직 관리", "CMN-FR-002", "/system/organizations"],
  ["M-ROLES", "역할 관리", "CMN-FR-005", "/system/roles"],
  ["M-UR", "사용자 역할 관리", "CMN-FR-006", "/system/user-roles"],
  ["M-MP", "메뉴 권한 관리", "CMN-FR-007", "/system/menu-permissions"],
  ["M-MS", "메뉴 구조 관리", "CMN-FR-013", "/system/menu-structure"],
  ["M-MI", "메뉴 정보 관리", "CMN-FR-014", "/system/menu-info"],
  ["M-CG", "코드그룹 관리", "CMN-FR-016", "/system/code-groups"],
  ["M-DC", "상세코드 관리", "CMN-FR-017", "/system/detail-codes"],
].map(([menuId, menuName, screenId, url], index) => ({
  menuId,
  menuName,
  screenId,
  url,
  parentMenuId: "SYS",
  menuLevel: 3,
  displayOrder: index + 1,
  activeYn: "Y",
}));

function response(data: unknown) {
  return Promise.resolve({
    ok: true,
    json: async () => ({ success: true, data }),
  });
}
function page(items: unknown[]) {
  return {
    items,
    page: 0,
    size: items.length,
    totalElements: items.length,
    totalPages: 1,
  };
}

beforeEach(() => {
  window.history.pushState({}, "", "/login");
  vi.stubGlobal(
    "fetch",
    vi.fn((url: string, init?: RequestInit) => {
      if (url === "/api/auth/me")
        return Promise.resolve({
          ok: false,
          status: 401,
          json: async () => ({
            success: false,
            error: { code: "UNAUTHORIZED", message: "로그인 필요" },
          }),
        });
      if (url === "/api/auth/login")
        return response({
          userId: "admin",
          username: "admin",
          roles: ["R09"],
          menus,
        });
      if (url.startsWith("/api/users"))
        return response(
          page([
            {
              userId: "u1",
              staffNo: "200001",
              staffName: "김교원",
              organizationCode: "DEP",
              positionName: "교수",
              employmentStatus: "ACTIVE",
              dutyName: "학과교수",
              lastSyncedAt: "2026-01-01T00:00:00",
              systemUseYn: "Y",
              roles: ["R01"],
            },
          ]),
        );
      if (url.startsWith("/api/organizations/tree"))
        return response([
          {
            organizationCode: "DEP",
            organizationName: "학과",
            organizationType: "DEPARTMENT",
            effectiveStartDate: "2026-01-01",
            useYn: "Y",
          },
        ]);
      if (url.startsWith("/api/organizations"))
        return response(
          page([
            {
              organizationCode: "DEP",
              organizationName: "학과",
              organizationType: "DEPARTMENT",
              effectiveStartDate: "2026-01-01",
              useYn: "Y",
            },
          ]),
        );
      if (url.startsWith("/api/roles"))
        return response([
          {
            roleCode: "R09",
            roleName: "시스템관리자",
            purpose: "관리",
            grantCriteria: "지정",
            dataScopeDefault: "전체",
            useYn: "Y",
          },
        ]);
      if (url.startsWith("/api/user-roles"))
        return response(
          page([
            {
              userRoleId: 1,
              userId: "u1",
              roleCode: "R01",
              assignmentType: "MANUAL",
              validFrom: "2026-01-01",
              approvedBy: "admin",
              useYn: "Y",
            },
          ]),
        );
      if (url.startsWith("/api/menu-permissions"))
        return response(
          page([
            {
              targetType: "ROLE",
              targetId: "R09",
              menuId: "M-USERS",
              accessAllowedYn: "Y",
              explicitDenyYn: "N",
            },
          ]),
        );
      if (url.startsWith("/api/menus/tree")) return response(menus);
      if (url.startsWith("/api/menus")) return response(page(menus));
      if (url.startsWith("/api/code-groups"))
        return response(
          page([
            {
              groupId: "USE_YN",
              groupName: "사용여부",
              managementDepartment: "시스템관리",
              useYn: "Y",
            },
          ]),
        );
      return response({});
    }),
  );
});

describe("system management routes", () => {
  it("logs in and renders all nine protected menu routes without 404", async () => {
    render(<App />);
    await userEvent.click(
      await screen.findByRole("button", { name: "로그인" }),
    );
    for (const menu of menus) {
      await userEvent.click(
        await screen.findByRole("link", { name: menu.menuName }),
      );
      expect(
        screen.getAllByRole("heading", { name: menu.menuName }).length,
      ).toBeGreaterThan(0);
      expect(screen.getByRole("link", { name: menu.menuName })).toHaveAttribute(
        "aria-current",
        "page",
      );
      expect(screen.queryByText("범위 밖 화면")).not.toBeInTheDocument();
    }
  });

  it("shows loading, success and editable management regions for user page", async () => {
    render(<App />);
    await userEvent.click(
      await screen.findByRole("button", { name: "로그인" }),
    );
    expect((await screen.findAllByText("사용자 목록")).length).toBeGreaterThan(
      0,
    );
    expect(await screen.findByText("김교원")).toBeInTheDocument();
    await userEvent.click(screen.getByText("김교원"));
    expect(screen.getByText("선택 사용자 상세/편집")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "사용여부 저장" }),
    ).toBeInTheDocument();
  });

  it("does not expose unauthorized menu when current user lacks menu grant", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn((url: string) => {
        if (url === "/api/auth/me")
          return Promise.resolve({
            ok: false,
            status: 401,
            json: async () => ({
              success: false,
              error: { code: "UNAUTHORIZED", message: "로그인 필요" },
            }),
          });
        if (url === "/api/auth/login")
          return response({
            userId: "u",
            username: "u",
            roles: ["R01"],
            menus: [menus[0]],
          });
        if (url.startsWith("/api/users")) return response(page([]));
        return response({});
      }),
    );
    render(<App />);
    await userEvent.click(
      await screen.findByRole("button", { name: "로그인" }),
    );
    await waitFor(() =>
      expect(
        screen.queryByRole("link", { name: "메뉴 권한 관리" }),
      ).not.toBeInTheDocument(),
    );
    window.history.pushState({}, "", "/system/menu-permissions");
    window.dispatchEvent(new PopStateEvent("popstate"));
    expect(await screen.findByText("권한 없음")).toBeInTheDocument();
  });
});
