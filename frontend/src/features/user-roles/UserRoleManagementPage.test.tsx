import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { UserRoleManagementPage } from "./UserRoleManagementPage";

const userRole = (
  overrides: Partial<{
    userRoleId: string;
    roleCode: string;
    approvalUserId: string;
    effectiveStartDate: string;
    effectiveEndDate: string | null;
    assignmentType: string;
    status: string;
  }> = {},
) => ({
  userRoleId: "USER-ROLE-MEMBER-R01",
  roleCode: "R01",
  approvalUserId: "admin",
  effectiveStartDate: "2026-08-09",
  effectiveEndDate: null,
  assignmentType: "MANUAL",
  status: "ACTIVE",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("UserRoleManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("grants a role and requeries approval user and effective period for the selected user", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([userRole()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() =>
        response([
          userRole(),
          userRole({
            userRoleId: "USER-ROLE-MEMBER-R02",
            roleCode: "R02",
            effectiveEndDate: "2026-12-31",
          }),
        ]),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<UserRoleManagementPage />);
    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "member" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("R01");

    fireEvent.click(screen.getByRole("button", { name: "역할 부여" }));
    fireEvent.change(screen.getByLabelText("역할코드"), {
      target: { value: "R02" },
    });
    fireEvent.change(screen.getByLabelText("승인자"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("유효 시작일"), {
      target: { value: "2026-08-09" },
    });
    fireEvent.change(screen.getByLabelText("유효 종료일"), {
      target: { value: "2026-12-31" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 현재 역할 목록을 다시 조회했습니다.");
    expect(screen.getByText("R02")).toBeInTheDocument();
    expect(screen.getAllByText("admin")).toHaveLength(2);
    expect(screen.getByText("2026-12-31")).toBeInTheDocument();
    expect(fetchMock.mock.calls[0][0]).toBe("/api/users/member/roles");
    expect(fetchMock.mock.calls[1][0]).toBe("/api/users/member/roles");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/users/member/roles");
  });

  it("renders the permission state instead of role controls after a forbidden role query", async () => {
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

    render(<UserRoleManagementPage />);
    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "member" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await waitFor(() =>
      expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument(),
    );
    expect(
      screen.queryByRole("button", { name: "역할 부여" }),
    ).not.toBeInTheDocument();
  });

  it("revokes the selected role and requeries the current role list", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([userRole()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response([]));
    vi.stubGlobal("fetch", fetchMock);

    render(<UserRoleManagementPage />);
    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "member" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("R01");
    fireEvent.click(screen.getByRole("button", { name: "회수" }));
    fireEvent.click(screen.getByTestId("user-role-revoke-confirm-button"));

    await screen.findByText("현재 역할이 없습니다.");
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/users/member/roles/USER-ROLE-MEMBER-R01",
    );
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "DELETE" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/users/member/roles");
  });
});
