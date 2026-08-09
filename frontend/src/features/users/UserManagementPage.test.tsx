import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { UserManagementPage } from "./UserManagementPage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("UserManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("shows the selected user as read-only source-backed information", async () => {
    const fetchMock = vi.fn().mockImplementationOnce(() =>
      response([
        {
          userId: "member",
          personnelNo: "MEMBER-0001",
          name: "예시 사용자",
          organization: "한국교원대학교",
          position: "교원",
          employmentStatus: "재직",
          roleCodes: ["R01"],
          useYn: "Y",
          positionTitle: null,
          retirementDate: null,
          lastSyncedAt: "2026-08-09T00:00:00Z",
        },
      ]),
    );
    vi.stubGlobal("fetch", fetchMock);

    render(<UserManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "검색" }));
    await screen.findByRole("button", { name: "예시 사용자 사용자 상세 열기" });

    fireEvent.click(
      screen.getByRole("button", { name: "예시 사용자 사용자 상세 열기" }),
    );
    expect(screen.getAllByText("R01")).not.toHaveLength(0);
    expect(
      screen.getByText(/KORUS 원천 인사 정보와 역할·사용여부 변경/),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "설정 저장" }),
    ).not.toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("renders a permission state instead of management controls after a forbidden response", async () => {
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

    render(<UserManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "검색" }));

    await waitFor(() =>
      expect(screen.getByText("권한이 없습니다.")).toBeInTheDocument(),
    );
    expect(screen.queryByLabelText("교번")).not.toBeInTheDocument();
  });
});
