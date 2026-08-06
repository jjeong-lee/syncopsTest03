import { describe, expect, it } from "vitest";
import { screens } from "../src/featureCatalog";

describe("feature catalog", () => {
  it("contains the 25 source-backed common screens and relative api paths", () => {
    expect(screens).toHaveLength(25);
    expect(screens.every((screen) => screen.apiPath.startsWith("/api/"))).toBe(
      true,
    );
    expect(screens.map((screen) => screen.route)).toContain(
      "/admin/cmn/fr/081",
    );
  });
});
