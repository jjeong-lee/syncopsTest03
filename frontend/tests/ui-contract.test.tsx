import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { ManagementPage } from "../src/app/components/ManagementPage";
import { screens } from "../src/app/screenConfigs";

const envelope = (data: unknown) => ({
  ok: true,
  json: async () => ({ success: true, data }),
});

describe("UI contract drift repairs", () => {
  it("keeps user usage and business-role saves as separate CTAs and payloads", async () => {
    const userScreen = screens.find((item) => item.id === "SCR-USER-MGMT");
    expect(userScreen).toBeDefined();
    const fetchMock = vi.fn(async (path: string, init?: RequestInit) => {
      if (path.startsWith("/api/users/u1/usage")) return envelope({});
      if (path.startsWith("/api/users/u1/roles")) return envelope({});
      if (path.startsWith("/api/users/u1")) {
        return envelope({
          userId: "u1",
          staffNo: "2024001",
          displayName: "김교수",
          systemEnabled: "Y",
          roleCodes: ["R01"],
        });
      }
      if (path.startsWith("/api/users")) {
        return envelope({
          items: [
            {
              userId: "u1",
              staffNo: "2024001",
              displayName: "김교수",
              systemEnabled: "Y",
              roleCodes: ["R01"],
            },
          ],
          page: 0,
          size: 20,
          totalElements: 1,
        });
      }
      return envelope({ items: [] });
    });
    vi.stubGlobal("fetch", fetchMock);

    render(
      <MemoryRouter initialEntries={["/system/users"]}>
        <ManagementPage screen={userScreen!} readonly={false} />
      </MemoryRouter>,
    );

    expect(await screen.findByText("사용여부 저장")).toBeInTheDocument();
    fireEvent.click(screen.getByText("사용여부 저장"));

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/users/u1/usage",
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ systemEnabled: "Y", reason: undefined }),
        }),
      ),
    );
    expect(
      fetchMock.mock.calls.some(
        ([path, init]) =>
          path === "/api/users/u1/roles" && init?.method === "PUT",
      ),
    ).toBe(false);

    fireEvent.click(screen.getByText("업무 역할 저장"));
    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/users/u1/roles",
        expect.objectContaining({
          method: "PUT",
          body: JSON.stringify({ roleCodes: ["R01"], reason: undefined }),
        }),
      ),
    );
  });

  it("locks roleCode in edit mode while preserving it in the update payload", async () => {
    const roleScreen = screens.find((item) => item.id === "SCR-ROLE-MGMT");
    expect(roleScreen).toBeDefined();
    const fetchMock = vi.fn(async (path: string, init?: RequestInit) => {
      if (path === "/api/roles/R01") return envelope({});
      if (path.startsWith("/api/roles")) {
        return envelope({
          items: [
            {
              roleCode: "R01",
              roleName: "교원",
              purpose: "교원 업적 입력",
              grantCriteria: "재직",
              defaultDataScope: "SELF",
              isActive: "Y",
            },
          ],
          page: 0,
          size: 20,
          totalElements: 1,
        });
      }
      return envelope({ items: [] });
    });
    vi.stubGlobal("fetch", fetchMock);

    render(
      <MemoryRouter initialEntries={["/system/roles"]}>
        <ManagementPage screen={roleScreen!} readonly={false} />
      </MemoryRouter>,
    );

    const roleCode = await waitFor(() =>
      document.querySelector<HTMLSelectElement>("#field-roleCode"),
    );
    expect(roleCode).not.toBeNull();
    expect(roleCode).toBeDisabled();

    fireEvent.click(screen.getByText("저장"));
    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/roles/R01",
        expect.objectContaining({
          method: "PUT",
          body: expect.stringContaining('"roleCode":"R01"'),
        }),
      ),
    );
  });
});
