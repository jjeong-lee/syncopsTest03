import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ReferenceYearManagementPage } from "./ReferenceYearManagementPage";

const settings = {
  currentEvaluationYear: 2026,
  defaultSearchYear: 2025,
  targetYear: 2027,
  referenceDataCopyYn: "Y",
  initializationYn: "N",
};

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(
      JSON.stringify(status < 400 ? { success: true, data, meta: {} } : data),
      { status },
    ),
  );

describe("SCR-REFERENCE-YEAR-MANAGEMENT", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads, confirms, saves, and requeries reference year settings without offering execution controls", async () => {
    const savedSettings = {
      ...settings,
      currentEvaluationYear: 2028,
      targetYear: 2029,
      initializationYn: "Y",
    };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(settings))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(savedSettings));
    vi.stubGlobal("fetch", fetchMock);

    render(<ReferenceYearManagementPage />);
    await screen.findByLabelText("현재 평가연도");
    fireEvent.change(screen.getByLabelText("현재 평가연도"), {
      target: { value: "2028" },
    });
    fireEvent.change(screen.getByLabelText("대상 연도"), {
      target: { value: "2029" },
    });
    fireEvent.change(screen.getByLabelText("초기화 여부"), {
      target: { value: "Y" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(
      screen.getByText(
        "실제 복사·초기화나 기존 연도 평가자료 변경은 수행하지 않습니다.",
      ),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: /복사 실행|초기화 실행/ }),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "확인 저장" }));
    await screen.findByText("저장 후 기준연도 설정값을 다시 조회했습니다.");

    expect(fetchMock.mock.calls[0][0]).toBe("/api/settings/reference-years");
    expect(fetchMock.mock.calls[1][0]).toBe("/api/settings/reference-years");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toEqual(savedSettings);
    expect(fetchMock.mock.calls[2][0]).toBe("/api/settings/reference-years");
    expect(screen.getByLabelText("대상 연도")).toHaveValue(2029);
  });

  it("blocks missing values before confirmation and renders a server field error after save", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(settings))
      .mockImplementationOnce(() =>
        response(
          {
            error: {
              field: "referenceDataCopyYn",
              message: "기준정보 복사 여부는 Y 또는 N이어야 합니다.",
            },
          },
          400,
        ),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<ReferenceYearManagementPage />);
    await screen.findByLabelText("대상 연도");
    fireEvent.change(screen.getByLabelText("대상 연도"), {
      target: { value: "" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    expect(screen.getByText("대상 연도은 필수입니다.")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);

    fireEvent.change(screen.getByLabelText("대상 연도"), {
      target: { value: "2027" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    fireEvent.click(screen.getByRole("button", { name: "확인 저장" }));
    await screen.findByText("기준정보 복사 여부는 Y 또는 N이어야 합니다.");
  });

  it("shows permission state without exposing reference year values or save controls", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(() => response({ error: { code: "FORBIDDEN" } }, 403)),
    );

    render(<ReferenceYearManagementPage />);
    await screen.findByText("권한이 없습니다.");
    expect(screen.queryByLabelText("현재 평가연도")).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "저장" }),
    ).not.toBeInTheDocument();
  });
});
