import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

describe("apiClient", () => {
  it("uses relative /api paths and contains no hardcoded host", () => {
    const source = readFileSync(
      new URL("../src/services/apiClient.ts", import.meta.url),
      "utf8",
    );
    const localHostToken = ["local", "host"].join("");
    const backendHttpToken = ["http://", "backend"].join("");
    expect(source).toContain("path: `/api/${string}`");
    expect(source).not.toContain(localHostToken);
    expect(source).not.toContain(backendHttpToken);
  });
});
