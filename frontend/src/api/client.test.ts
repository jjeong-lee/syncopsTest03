import { describe, expect, it } from "vitest";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";

function files(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? files(path) : [path];
  });
}

describe("frontend API boundary", () => {
  it("uses relative /api paths and does not hardcode localhost or service names", () => {
    const source = files("src")
      .filter(
        (path) => !path.endsWith(".test.ts") && !path.endsWith(".test.tsx"),
      )
      .map((path) => readFileSync(path, "utf8"))
      .join("\n");
    expect(source).toMatch(/API_PREFIX\s*=\s*["']\/api["']/);
    expect(source).not.toMatch(/https?:\/\/localhost|backend:|database:/);
  });
});
