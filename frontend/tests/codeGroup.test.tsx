import { describe, expect, it } from "vitest";
import { adminRoutes } from "../src/app/routes";

describe("codeGroup", () => {
  it("maps route to relative api path and required UI states", () => {
    const route = adminRoutes.find(
      (item) => item.path === "/system/code-groups",
    );
    expect(route).toBeTruthy();
    expect(route!.apiPath.startsWith("/api/")).toBe(true);
    expect(route!.fields.length).toBeGreaterThan(3);
  });
});
