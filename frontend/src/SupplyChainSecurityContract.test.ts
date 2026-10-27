import { describe, expect, it } from "vitest";
// @ts-expect-error Vite transforms the Dockerfile raw import in test and build environments.
import dockerfile from "../Dockerfile?raw";

describe("supply-chain security contract", () => {
  it("uses a patched nginx Alpine base image", () => {
    expect(dockerfile).toContain("nginx:1.29.2-alpine");
    expect(dockerfile).toContain("RUN apk upgrade --no-cache");
  });
});
