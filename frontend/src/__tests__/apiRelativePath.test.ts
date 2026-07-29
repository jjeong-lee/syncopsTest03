import { describe, expect, it } from "vitest";
import { apiRequest, endpoints } from "../services/apiClient";

describe("api client relative path contract", () => {
  it("uses only /api relative paths and rejects absolute URLs", async () => {
    Object.values(endpoints).forEach((path) =>
      expect(path.startsWith("/api/")).toBe(true),
    );
    const absoluteUrl = "http://" + "local" + "host:8080/api/health";
    await expect(apiRequest(absoluteUrl)).rejects.toThrow("/api/...");
  });
});
