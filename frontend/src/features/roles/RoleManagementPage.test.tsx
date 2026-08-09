import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { RoleManagementPage } from "./RoleManagementPage";

const role = (
  overrides: Partial<{
    roleCode: string;
    roleName: string;
    purpose: string;
    assignmentCriteria: string | null;
    defaultDataScope: string | null;
  }> = {},
) => ({
  roleCode: "R09",
  roleName: "시스템관리자",
  purpose: "사용자·조직·메뉴·권한·코드를 관리하는 관리자 역할",
  assignmentCriteria: null,
  defaultDataScope: null,
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("RoleManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("keeps the API role code read-only and requeries the role list after saving criteria and data scope", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([role()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() =>
        response([
          role({
            roleName: "시스템 운영 관리자",
            assignmentCriteria: "시스템 운영 담당자",
            defaultDataScope: "시스템 관리 전체",
          }),
        ]),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<RoleManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "역할 목록 조회" }));
    await screen.findByRole("button", { name: "R09 역할 선택" });

    fireEvent.click(screen.getByRole("button", { name: "R09 역할 선택" }));
    expect(screen.getByDisplayValue("R09")).toHaveAttribute("readonly");
    fireEvent.change(screen.getByLabelText("역할명"), {
      target: { value: "시스템 운영 관리자" },
    });
    fireEvent.change(screen.getByLabelText("부여 기준"), {
      target: { value: "시스템 운영 담당자" },
    });
    fireEvent.change(screen.getByLabelText("데이터 범위 기본값"), {
      target: { value: "시스템 관리 전체" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장한 역할 정보를 다시 조회했습니다.");
    expect(screen.getByDisplayValue("R09")).toBeInTheDocument();
    expect(screen.getByDisplayValue("시스템 운영 관리자")).toBeInTheDocument();
    expect(fetchMock.mock.calls[1][0]).toBe("/api/roles");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toContain("/api/roles");
  });

  it("renders permission state instead of role controls after a forbidden response", async () => {
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

    render(<RoleManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "역할 목록 조회" }));

    await waitFor(() =>
      expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument(),
    );
    expect(
      screen.queryByRole("button", { name: "역할 목록 조회" }),
    ).not.toBeInTheDocument();
  });

  it("shows a save ApiError next to the field identified by the API", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([role()]))
      .mockImplementationOnce(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              success: false,
              error: { field: "roleName", message: "역할명은 필수입니다." },
              meta: {},
            }),
            { status: 400 },
          ),
        ),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<RoleManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "역할 목록 조회" }));
    await screen.findByRole("button", { name: "R09 역할 선택" });
    fireEvent.click(screen.getByRole("button", { name: "R09 역할 선택" }));
    fireEvent.change(screen.getByLabelText("역할명"), {
      target: { value: "" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("역할명은 필수입니다.");
    expect(screen.getByLabelText("역할명").parentElement).toHaveTextContent(
      "역할명은 필수입니다.",
    );
  });
});
