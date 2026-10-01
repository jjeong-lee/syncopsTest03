import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CodeUsageManagementPage } from "./CodeUsageManagementPage";

const detailCode = (
  overrides: Partial<{
    detailCodeId: string;
    codeValue: string;
    codeName: string;
    parentDetailCodeId: string | null;
    displayOrder: number;
    additionalAttributes: Record<string, string> | null;
    useYn: string;
    applicationStartDate: string;
    applicationEndDate: string | null;
  }> = {},
) => ({
  detailCodeId: "DETAIL-CODE-HISTORICAL",
  codeValue: "HISTORICAL",
  codeName: "과거 코드",
  parentDetailCodeId: null,
  displayOrder: 1,
  additionalAttributes: null,
  useYn: "N",
  applicationStartDate: "2020-01-01",
  applicationEndDate: "2020-12-31",
  ...overrides,
});

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: status < 400, data, meta: {} }), {
      status,
    }),
  );

describe("SCR-CODE-USAGE-MANAGEMENT", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("queries ended codes, confirms a reactivation, and requeries the saved usage period", async () => {
    const reactivatedCode = detailCode({
      useYn: "Y",
      applicationEndDate: "2099-12-31",
    });
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([detailCode()]))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response([reactivatedCode]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    render(<CodeUsageManagementPage />);
    fireEvent.change(screen.getByLabelText("코드그룹 ID"), {
      target: { value: "CG-CODE-USAGE" },
    });
    fireEvent.click(screen.getByLabelText("종료·중지 코드 포함"));
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "사용기간 수정" });

    fireEvent.click(screen.getByRole("button", { name: "사용기간 수정" }));
    fireEvent.change(screen.getByLabelText("사용여부"), {
      target: { value: "Y" },
    });
    fireEvent.change(screen.getByLabelText("적용 종료일"), {
      target: { value: "2099-12-31" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText(
      "저장 후 코드 사용여부와 적용기간을 다시 조회했습니다.",
    );
    expect(window.confirm).toHaveBeenCalledWith(
      "코드 사용여부와 적용기간을 저장하시겠습니까?",
    );
    expect(fetchMock.mock.calls[0][0]).toBe(
      "/api/code-groups/CG-CODE-USAGE/detail-codes?includeEnded=true",
    );
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/code-groups/CG-CODE-USAGE/detail-codes",
    );
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe(
      "/api/code-groups/CG-CODE-USAGE/detail-codes?includeEnded=true",
    );
    expect(screen.getByText("2099-12-31")).toBeInTheDocument();
  });

  it("keeps the edit local when confirmation is cancelled", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementation(() => response([detailCode()]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => false),
    );

    render(<CodeUsageManagementPage />);
    fireEvent.change(screen.getByLabelText("코드그룹 ID"), {
      target: { value: "CG-CODE-USAGE" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "사용기간 수정" });
    fireEvent.click(screen.getByRole("button", { name: "사용기간 수정" }));
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).toBeInTheDocument();
  });

  it("shows field errors after a failed save and permission state after a forbidden query", async () => {
    const saveFailure = vi
      .fn()
      .mockImplementationOnce(() => response([detailCode()]))
      .mockImplementationOnce(() =>
        response({ error: { field: "applicationStartDate" } }, 400),
      );
    vi.stubGlobal("fetch", saveFailure);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    const { unmount } = render(<CodeUsageManagementPage />);
    fireEvent.change(screen.getByLabelText("코드그룹 ID"), {
      target: { value: "CG-CODE-USAGE" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByRole("button", { name: "사용기간 수정" });
    fireEvent.click(screen.getByRole("button", { name: "사용기간 수정" }));
    fireEvent.change(screen.getByLabelText("적용 시작일"), {
      target: { value: "" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    await screen.findByText("적용 시작일은 필수입니다.");
    unmount();

    vi.stubGlobal(
      "fetch",
      vi.fn(() => response({ error: { code: "FORBIDDEN" } }, 403)),
    );
    render(<CodeUsageManagementPage />);
    fireEvent.change(screen.getByLabelText("코드그룹 ID"), {
      target: { value: "CG-CODE-USAGE" },
    });
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "사용기간 수정" }),
    ).not.toBeInTheDocument();
  });
});
