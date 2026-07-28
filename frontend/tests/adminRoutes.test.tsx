import { describe, expect, it } from "vitest";
import { adminRoutes } from "../src/app/routes";

describe("admin routes", () => {
  it("declares the 9 management routes from ui-design.md", () => {
    expect(adminRoutes.map((route) => route.path)).toEqual([
      "/system/users",
      "/system/organizations",
      "/system/roles",
      "/system/user-roles",
      "/system/menu-permissions",
      "/system/menu-structure",
      "/system/menu-info",
      "/system/code-groups",
      "/system/codes",
    ]);
  });
});
