import { describe, expect, it } from "vitest";
import { screens, navigationGroups } from "../src/screens";
import { allowedPayload } from "../src/AppRouter";
import { buildListUrl } from "../src/services/api";

const expectedRoutes = [
  "/admin/users",
  "/admin/organizations",
  "/admin/positions",
  "/admin/roles",
  "/admin/user-roles",
  "/admin/menu-permissions",
  "/admin/function-permissions",
  "/admin/data-scope-permissions",
  "/admin/menus",
  "/admin/code-groups",
  "/admin/codes",
  "/admin/system-settings",
  "/admin/base-years",
  "/admin/file-policies",
  "/admin/notices",
  "/admin/attachments",
  "/admin/upload-templates",
  "/admin/excel-uploads",
  "/admin/excel-downloads",
  "/admin/personal-information",
  "/admin/active-sessions",
  "/admin/audit-logs",
  "/admin/batch-definitions",
  "/admin/batch-executions",
  "/admin/batch-results",
];

describe("UI route contract", () => {
  it("contains all 25 protected management screens with relative API paths", () => {
    expect(screens).toHaveLength(25);
    expect(screens.map((screen) => screen.route)).toEqual(expectedRoutes);
    expect(screens.every((screen) => screen.route.startsWith("/admin/"))).toBe(
      true,
    );
    expect(screens.every((screen) => screen.apiPath.startsWith("/api/"))).toBe(
      true,
    );
    expect(JSON.stringify(screens)).not.toContain("localhost");
  });

  it("keeps the three-level sidebar information architecture from ui-design", () => {
    expect(navigationGroups.map((group) => group.topMenu)).toEqual([
      "시스템 관리",
      "파일·데이터 관리",
      "보안·감사 관리",
      "시스템 운영 관리",
    ]);
    expect(
      navigationGroups
        .flatMap((group) => group.middleMenus.flatMap((middle) => middle.items))
        .map((screen) => screen.route),
    ).toEqual(expectedRoutes);
  });

  it("defines API-backed columns, editable fields, and UI state checklist for every page", () => {
    for (const screen of screens) {
      expect(screen.columns.length).toBeGreaterThan(0);
      expect(screen.goal).toContain(".");
      expect(screen.primaryCta.length).toBeGreaterThan(0);
      expect(screen.actions.length).toBeGreaterThan(0);
      if (!screen.noMutation) {
        expect(screen.editableFields.some((field) => field.key === "id")).toBe(
          true,
        );
      }
    }
  });

  it("does not expose generic create mode where ui-design only allows selecting existing lifecycle records", () => {
    const noGenericCreateScreens = [
      "SCR-001-users",
      "SCR-002-organizations",
      "SCR-006-menu-permissions",
      "SCR-007-function-permissions",
      "SCR-008-data-scope-permissions",
      "SCR-012-system-settings",
      "SCR-016-attachments",
      "SCR-019-excel-downloads",
      "SCR-020-personal-information",
      "SCR-021-active-sessions",
      "SCR-024-batch-executions",
    ];

    expect(
      screens
        .filter((screen) => noGenericCreateScreens.includes(screen.id))
        .every((screen) => screen.supportsCreate === false),
    ).toBe(true);
  });

  it("keeps non-keyword search fields in the list API query instead of treating them as local-only URL state", () => {
    expect(
      buildListUrl("/api/users", "kim", 20, 0, {
        systemUseYn: "Y",
        emptyIgnored: "",
      }),
    ).toBe("/api/users?keyword=kim&size=20&page=0&systemUseYn=Y");
  });

  it("saves only user-account editable fields for SCR-001 and excludes KORUS source fields", () => {
    const users = screens.find((screen) => screen.id === "SCR-001-users")!;
    const payload = allowedPayload(
      users,
      {
        id: "U001",
        title: "홍길동",
        employeeNo: "E001",
        organizationCode: "ORG001",
        rankName: "교수",
        employmentStatus: "ACTIVE",
        systemUseYn: "N",
        primaryRoleCode: "R09",
      },
      {
        id: "U001",
        title: "홍길동",
        systemUseYn: "Y",
        primaryRoleCode: "R01",
      },
    );

    expect(payload).toMatchObject({
      id: "U001",
      title: "홍길동",
      systemUseYn: "N",
      primaryRoleCode: "R09",
    });
    expect(payload).not.toHaveProperty("employeeNo");
    expect(payload).not.toHaveProperty("organizationCode");
    expect(payload).not.toHaveProperty("rankName");
    expect(payload).not.toHaveProperty("employmentStatus");
  });
});
