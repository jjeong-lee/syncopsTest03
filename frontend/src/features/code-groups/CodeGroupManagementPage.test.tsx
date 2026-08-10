import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CodeGroupManagementPage } from "./CodeGroupManagementPage";

const codeGroup = (
  overrides: Partial<{
    groupId: string;
    groupName: string;
    description: string | null;
    managementDepartment: string | null;
    useYn: string;
  }> = {},
) => ({
  groupId: "CG-ACADEMIC-STATUS",
  groupName: "학사 상태",
  description: "학사 상태 공통코드",
  managementDepartment: "교수지원과",
  useYn: "Y",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("CodeGroupManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("requeries and redisplays a code group after saving its editable fields", async () => {
    const initialGroups = [codeGroup()];
    const savedGroups = [
      codeGroup({
        groupName: "학사 상태 코드",
        description: "수정된 학사 상태 공통코드",
        managementDepartment: "학사관리과",
      }),
    ];
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(initialGroups))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(savedGroups));
    vi.stubGlobal("fetch", fetchMock);

    render(<CodeGroupManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("학사 상태");

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(screen.getByLabelText("명칭"), {
      target: { value: "학사 상태 코드" },
    });
    fireEvent.change(screen.getByLabelText("설명"), {
      target: { value: "수정된 학사 상태 공통코드" },
    });
    fireEvent.change(screen.getByLabelText("관리부서"), {
      target: { value: "학사관리과" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 코드그룹을 다시 조회했습니다.");
    expect(screen.getAllByText("학사 상태 코드")).not.toHaveLength(0);
    expect(screen.getAllByText("수정된 학사 상태 공통코드")).not.toHaveLength(
      0,
    );
    expect(screen.getAllByText("학사관리과")).not.toHaveLength(0);
    expect(fetchMock.mock.calls[1][0]).toBe("/api/code-groups");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/code-groups");
  });

  it("provides a detail code management link with the selected group context", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(() => response([codeGroup()])),
    );
    render(<CodeGroupManagementPage />);

    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("학사 상태");
    fireEvent.click(screen.getByText("학사 상태"));
    expect(screen.getByRole("link", { name: "상세코드 목록" })).toHaveAttribute(
      "href",
      "/system/common-codes/detail-codes?groupId=CG-ACADEMIC-STATUS",
    );
  });

  it("shows permission state instead of code group controls after a forbidden response", async () => {
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
    render(<CodeGroupManagementPage />);

    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "코드그룹 등록" }),
    ).not.toBeInTheDocument();
  });
});
