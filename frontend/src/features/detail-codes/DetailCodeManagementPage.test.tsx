import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DetailCodeManagementPage } from "./DetailCodeManagementPage";

const detailCode = (
  overrides: Partial<{
    detailCodeId: string;
    codeValue: string;
    codeName: string;
    parentDetailCodeId: string | null;
    displayOrder: number;
    additionalAttributes: Record<string, string> | null;
    useYn: string;
  }> = {},
) => ({
  detailCodeId: "DETAIL-CODE-CHILD",
  codeValue: "CHILD",
  codeName: "하위 코드",
  parentDetailCodeId: "DETAIL-CODE-PARENT",
  displayOrder: 2,
  additionalAttributes: { mappingKey: "CHILD-MAP" },
  useYn: "Y",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

describe("DetailCodeManagementPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/system/common-codes/detail-codes");
  });

  it("requeries and redisplays hierarchy, order, and additional attributes after saving a detail code", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/common-codes/detail-codes?groupId=CG-ACADEMIC-STATUS",
    );
    const initialCodes = [detailCode()];
    const savedCodes = [
      detailCode({
        codeName: "수정된 하위 코드",
        displayOrder: 3,
        additionalAttributes: { mappingKey: "CHILD-MAP-UPDATED" },
      }),
    ];
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(initialCodes))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(savedCodes));
    vi.stubGlobal("fetch", fetchMock);

    render(<DetailCodeManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("하위 코드");

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(screen.getByLabelText("코드명"), {
      target: { value: "수정된 하위 코드" },
    });
    fireEvent.change(screen.getByLabelText("정렬순서"), {
      target: { value: "3" },
    });
    fireEvent.change(screen.getByLabelText("추가속성"), {
      target: { value: '{"mappingKey":"CHILD-MAP-UPDATED"}' },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 상세코드 목록을 다시 조회했습니다.");
    expect(screen.getAllByText("수정된 하위 코드")).not.toHaveLength(0);
    expect(screen.getAllByText("DETAIL-CODE-PARENT")).not.toHaveLength(0);
    expect(screen.getAllByText("3")).not.toHaveLength(0);
    expect(
      screen.getAllByText('{"mappingKey":"CHILD-MAP-UPDATED"}'),
    ).not.toHaveLength(0);
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/code-groups/CG-ACADEMIC-STATUS/detail-codes",
    );
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe(
      "/api/code-groups/CG-ACADEMIC-STATUS/detail-codes",
    );
  });

  it("does not request detail codes and only provides code group navigation without a group context", () => {
    const fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);

    render(<DetailCodeManagementPage />);

    expect(
      screen.getByText("코드그룹을 선택해야 상세코드를 조회할 수 있습니다."),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "코드그룹 관리로 이동" }),
    ).toHaveAttribute("href", "/system/common-codes/groups");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("shows permission state instead of detail code controls after a forbidden response", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/common-codes/detail-codes?groupId=CG-ACADEMIC-STATUS",
    );
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

    render(<DetailCodeManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "상세코드 등록" }),
    ).not.toBeInTheDocument();
  });
});
