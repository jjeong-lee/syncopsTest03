import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { OrganizationManagementPage } from "./OrganizationManagementPage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

const organization = (effectiveEndDate: string | null) => ({
  organizationId: "ORG-KNUE-EDU",
  organizationCode: "KNUE-EDU",
  organizationName: "교육대학원",
  organizationType: "GRADUATE_SCHOOL",
  parentOrganizationId: "ORG-KNUE",
  parentOrganizationCode: "KNUE",
  parentOrganizationName: "한국교원대학교",
  effectiveStartDate: "2026-01-01",
  effectiveEndDate,
  children: [],
});

describe("OrganizationManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders the selected organization hierarchy and requeries after relationship save", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([organization(null)]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response([organization("2026-12-31")]));
    vi.stubGlobal("fetch", fetchMock);

    render(<OrganizationManagementPage />);
    fireEvent.change(screen.getByLabelText("조직코드"), {
      target: { value: "KNUE-EDU" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "교육대학원 조직 선택" });

    fireEvent.click(
      screen.getByRole("button", { name: "교육대학원 조직 선택" }),
    );
    expect(screen.getAllByText("한국교원대학교")).not.toHaveLength(0);
    fireEvent.change(screen.getByLabelText("적용 종료일"), {
      target: { value: "2026-12-31" },
    });
    fireEvent.click(screen.getByRole("button", { name: "관계·적용기간 저장" }));

    await screen.findByText("저장한 조직 관계를 다시 조회했습니다.");
    expect(screen.getByDisplayValue("2026-12-31")).toBeInTheDocument();
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/organizations/ORG-KNUE-EDU/relationship",
    );
    expect(fetchMock.mock.calls[2][0]).toContain(
      "/api/organizations?organizationCode=KNUE-EDU",
    );
  });

  it("renders permission state instead of organization controls after a forbidden response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "FORBIDDEN" },
            meta: {},
          }),
          { status: 403 },
        ),
      ),
    );

    render(<OrganizationManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await waitFor(() =>
      expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument(),
    );
    expect(screen.queryByLabelText("조직코드")).not.toBeInTheDocument();
  });
});
