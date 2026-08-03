import { describe, expect, it } from "vitest";
import { apiRequest } from "../src/api/client";

describe("relative API client", () => {
  it("rejects absolute urls and docker service names", async () => {
    await expect(
      apiRequest("http://localhost:8080/api/health"),
    ).rejects.toThrow(/relative/);
    await expect(apiRequest("/api/backend:8080/users")).rejects.toThrow(
      /Absolute/,
    );
  });
});
