import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CommonEnvironmentSettingsPage } from "./CommonEnvironmentSettingsPage";

const settings = {
  sessionIdleMinutes: 60,
  pageSize: 20,
  defaultSearchPeriodDays: 14,
  bulkQueryThreshold: 1000,
  longRunningWorkNoticeSeconds: 60,
};

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(
      JSON.stringify(status < 400 ? { success: true, data, meta: {} } : data),
      { status },
    ),
  );

describe("SCR-COMMON-ENVIRONMENT-SETTINGS", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("queries settings, requires confirmation, saves, and requeries the persisted values", async () => {
    const savedSettings = { ...settings, sessionIdleMinutes: 45, pageSize: 50 };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(settings))
      .mockImplementationOnce(() => response(null))
      .mockImplementationOnce(() => response(savedSettings));
    vi.stubGlobal("fetch", fetchMock);

    render(<CommonEnvironmentSettingsPage />);
    await screen.findByLabelText("세션 유휴시간(분)");
    fireEvent.change(screen.getByLabelText("세션 유휴시간(분)"), {
      target: { value: "45" },
    });
    fireEvent.change(screen.getByLabelText("페이지당 조회건수"), {
      target: { value: "50" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(
      screen.getByText("이미 열린 세션의 유휴시간은 변경하지 않습니다."),
    ).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "확인 저장" }));
    await screen.findByText("저장 후 공통 환경설정 값을 다시 조회했습니다.");

    expect(fetchMock.mock.calls[0][0]).toBe("/api/settings/common");
    expect(fetchMock.mock.calls[1][0]).toBe("/api/settings/common");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(fetchMock.mock.calls[2][0]).toBe("/api/settings/common");
    expect(screen.getByLabelText("페이지당 조회건수")).toHaveValue("50");
  });

  it("blocks missing values before opening confirmation and renders a server field error after save", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response(settings))
      .mockImplementationOnce(() =>
        response(
          {
            error: {
              field: "pageSize",
              message: "페이지당 조회건수는 20, 50, 100만 가능합니다.",
            },
          },
          400,
        ),
      );
    vi.stubGlobal("fetch", fetchMock);

    render(<CommonEnvironmentSettingsPage />);
    await screen.findByLabelText("세션 유휴시간(분)");
    fireEvent.change(screen.getByLabelText("기본 검색기간(일)"), {
      target: { value: "" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    expect(
      screen.getByText("기본 검색기간(일)은 필수입니다."),
    ).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);

    fireEvent.change(screen.getByLabelText("기본 검색기간(일)"), {
      target: { value: "14" },
    });
    fireEvent.change(screen.getByLabelText("페이지당 조회건수"), {
      target: { value: "50" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    fireEvent.click(screen.getByRole("button", { name: "확인 저장" }));
    await screen.findByText("페이지당 조회건수는 20, 50, 100만 가능합니다.");
  });

  it("shows the permission state without exposing settings controls", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(() => response({ error: { code: "FORBIDDEN" } }, 403)),
    );

    render(<CommonEnvironmentSettingsPage />);
    await screen.findByText("권한이 없습니다.");
    expect(
      screen.queryByRole("button", { name: "저장" }),
    ).not.toBeInTheDocument();
  });
});
