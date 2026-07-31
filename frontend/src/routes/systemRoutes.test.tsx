import { describe, expect, it } from "vitest";

const targetRoutes = [
  "/system/users",
  "/system/organizations",
  "/system/roles",
  "/system/user-roles",
  "/system/menu-permissions",
  "/system/menu-structure",
  "/system/menu-info",
  "/system/code-groups",
  "/system/detail-codes",
];

describe("system route inventory", () => {
  it("keeps the nine first-scope management routes as vertical slices", () => {
    expect(targetRoutes).toHaveLength(9);
    expect(targetRoutes).not.toContain("/system/audit-logs");
    expect(targetRoutes).not.toContain("/system/files");
  });
});
