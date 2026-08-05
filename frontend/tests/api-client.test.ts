import { describe, expect, it, vi } from "vitest";
import { api } from "../src/api/client";

describe("api client", () => {
  it("uses relative /api paths only", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true, data: { ok: true } }),
    });
    vi.stubGlobal("fetch", fetchMock);
    await api("/api/health");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/health",
      expect.objectContaining({ credentials: "include" }),
    );
  });
});
