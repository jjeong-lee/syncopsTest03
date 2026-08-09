import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuPermissionManagementPage } from "./MenuPermissionManagementPage";

const permission = (
  overrides: Partial<{
    menuPermissionId: string;
    subjectType: string;
    subjectId: string;
    menuId: string;
    majorMenuName: string | null;
    middleMenuName: string | null;
    screenName: string | null;
    accessAllowed: string;
  }> = {},
) => ({
  menuPermissionId: "PERMISSION-R09-MENU-USER-MANAGEMENT",
  subjectType: "ROLE",
  subjectId: "R09",
  menuId: "MENU-USER-MANAGEMENT",
  majorMenuName: "시스템 관리",
  middleMenuName: "사용자·조직 관리",
  screenName: "사용자 관리",
  accessAllowed: "Y",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("MenuPermissionManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("queries a selected subject and requeries the permission matrix after saving access", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([permission()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() =>
        response([permission({ accessAllowed: "N" })]),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<MenuPermissionManagementPage />);
    fireEvent.change(screen.getByLabelText("대상 구분"), {
      target: { value: "ROLE" },
    });
    fireEvent.change(screen.getByLabelText("대상 ID"), {
      target: { value: "R09" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("사용자 관리");
    fireEvent.click(screen.getByRole("button", { name: "권한 설정" }));
    fireEvent.change(screen.getByLabelText("접근 허용 여부"), {
      target: { value: "N" },
    });
    fireEvent.change(screen.getByLabelText("사유"), {
      target: { value: "권한 검토 결과 차단" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 메뉴 권한 목록을 다시 조회했습니다.");
    expect(screen.getByText("N")).toBeInTheDocument();
    expect(fetchMock.mock.calls[0][0]).toBe(
      "/api/menu-permissions?subjectType=ROLE&subjectId=R09",
    );
    expect(fetchMock.mock.calls[1][0]).toBe("/api/menu-permissions");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "PUT" });
    expect(fetchMock.mock.calls[2][0]).toBe(
      "/api/menu-permissions?subjectType=ROLE&subjectId=R09",
    );
  });

  it("renders permission state rather than query and save controls after a forbidden response", async () => {
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

    render(<MenuPermissionManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await waitFor(() =>
      expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument(),
    );
    expect(
      screen.queryByRole("button", { name: "권한 설정" }),
    ).not.toBeInTheDocument();
  });
});
