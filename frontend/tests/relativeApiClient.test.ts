import { describe, expect, it, vi } from "vitest";
import { authApi } from "../src/api/domain";

describe("relative API client contract", () => {
  it("uses browser-relative /api paths and rejects absolute host names", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        success: true,
        data: { userId: "u", username: "u", roles: [], menus: [] },
      }),
    });
    vi.stubGlobal("fetch", fetchMock);
    await authApi.me();
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/auth/me",
      expect.objectContaining({ credentials: "include" }),
    );
    const source = JSON.stringify(fetchMock.mock.calls);
    expect(source).not.toContain("localhost");
    expect(source).not.toContain("backend:");
    vi.unstubAllGlobals();
  });
});
