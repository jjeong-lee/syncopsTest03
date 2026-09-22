import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ReferenceYearSettingsPage } from "./ReferenceYearSettingsPage";

const success = (data: unknown) =>
  new Response(JSON.stringify({ success: true, data, meta: {} }), {
    status: 200,
  });
const error = (status: number, message: string) =>
  new Response(
    JSON.stringify({
      success: false,
      error: { code: "REQUEST_ERROR", message },
      meta: {},
    }),
    { status },
  );
const row = {
  currentEvaluationYear: 2026,
  defaultQueryYear: 2025,
  targetYear: 2027,
  baselineCopyYn: "예",
  initializationYn: "아니오",
};

describe("ReferenceYearSettingsPage", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("loads the list and saves only after confirmation before requerying", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(success([row]))
      .mockResolvedValueOnce(success(row))
      .mockResolvedValueOnce(success([row]));
    vi.stubGlobal("fetch", fetchMock);
    vi.spyOn(window, "confirm").mockReturnValue(true);
    render(<ReferenceYearSettingsPage />);

    fireEvent.click(screen.getByTestId("reference-year-query-button"));
    fireEvent.click(await screen.findByTestId("reference-year-edit-2027"));
    fireEvent.click(screen.getByTestId("reference-year-save-button"));

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/system/settings/reference-years",
        expect.objectContaining({ method: "POST", body: JSON.stringify(row) }),
      ),
    );
    expect(
      await screen.findByText("저장 후 기준연도 설정을 다시 조회했습니다."),
    ).toBeInTheDocument();
    expect(
      fetchMock.mock.calls.filter(([url]) =>
        String(url).startsWith("/api/system/settings/reference-years?"),
      ).length,
    ).toBe(2);
  });

  it("does not save when the confirmation is cancelled", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValueOnce(success([row])));
    vi.spyOn(window, "confirm").mockReturnValue(false);
    render(<ReferenceYearSettingsPage />);
    fireEvent.click(screen.getByTestId("reference-year-query-button"));
    fireEvent.click(await screen.findByTestId("reference-year-edit-2027"));
    fireEvent.click(screen.getByTestId("reference-year-save-button"));
    expect(fetch).toHaveBeenCalledTimes(1);
  });

  it.each([
    ["loading", new Promise<Response>(() => {})],
    ["empty", success([])],
    ["error", error(500, "조회 실패")],
    ["permission", error(403, "권한 없음")],
  ])("renders the %s state", async (state, response) => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValueOnce(response));
    render(<ReferenceYearSettingsPage />);
    fireEvent.click(screen.getByTestId("reference-year-query-button"));
    if (state === "loading")
      expect(
        screen.getByText("기준연도 설정을 조회하고 있습니다."),
      ).toBeInTheDocument();
    if (state === "empty")
      expect(
        await screen.findByText("현재 조건에 맞는 기준연도 설정이 없습니다."),
      ).toBeInTheDocument();
    if (state === "error")
      expect(
        await screen.findByTestId("reference-year-retry-button"),
      ).toBeInTheDocument();
    if (state === "permission")
      expect(
        await screen.findByText(
          "권한이 없어 기준연도 설정을 조회하거나 변경할 수 없습니다.",
        ),
      ).toBeInTheDocument();
  });
});
