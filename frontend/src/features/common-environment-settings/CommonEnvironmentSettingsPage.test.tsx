import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CommonEnvironmentSettingsPage } from "./CommonEnvironmentSettingsPage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

const pageSizeSetting = { settingKey: "page_size", settingValue: "20" };

describe("CommonEnvironmentSettingsPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("saves a selected setting only after confirmation and requeries the last criteria", async () => {
    const savedSetting = { ...pageSizeSetting, settingValue: "50" };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([pageSizeSetting]))
      .mockImplementationOnce(() => response(savedSetting))
      .mockImplementationOnce(() => response([savedSetting]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    render(<CommonEnvironmentSettingsPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("page_size");

    fireEvent.click(screen.getByRole("button", { name: "변경" }));
    fireEvent.change(
      screen.getByTestId("common-environment-setting-value-input"),
      {
        target: { value: "50" },
      },
    );
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText("저장 후 공통 환경설정을 다시 조회했습니다.");
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/system/settings/common-environment",
    );
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toEqual(savedSetting);
    expect(fetchMock.mock.calls[2][0]).toContain("size=20");
    expect(screen.getAllByText("50")).not.toHaveLength(0);
  });

  it("keeps the edit modal open with a field error when setting value is empty", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(response([pageSizeSetting])),
    );
    render(<CommonEnvironmentSettingsPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("page_size");
    fireEvent.click(screen.getByRole("button", { name: "변경" }));
    fireEvent.change(
      screen.getByTestId("common-environment-setting-value-input"),
      {
        target: { value: "" },
      },
    );
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(screen.getByText("설정값은 필수입니다.")).toBeInTheDocument();
    expect(
      screen.getByTestId("common-environment-settings-edit-modal"),
    ).toBeInTheDocument();
  });

  it("shows the permission state after a forbidden environment query", async () => {
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
    render(<CommonEnvironmentSettingsPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText(
      "권한이 없어 공통 환경설정을 조회하거나 변경할 수 없습니다.",
    );
    expect(
      screen.queryByRole("button", { name: "변경" }),
    ).not.toBeInTheDocument();
  });

  it("shows a loading notice while a common environment query is pending", () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => new Promise<Response>(() => undefined))
      .mockImplementationOnce(() => response([]))
      .mockImplementationOnce(
        () =>
          new Response(
            JSON.stringify({
              success: false,
              error: { code: "INTERNAL_ERROR" },
              meta: {},
            }),
            { status: 500 },
          ),
      );
    vi.stubGlobal("fetch", fetchMock);
    render(<CommonEnvironmentSettingsPage />);

    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    expect(
      screen.getByText("공통 환경설정을 조회하고 있습니다."),
    ).toBeInTheDocument();
  });
});
