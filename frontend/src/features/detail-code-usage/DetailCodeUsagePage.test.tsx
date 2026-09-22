import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DetailCodeUsagePage } from "./DetailCodeUsagePage";

const response = (data: unknown, status = 200) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), { status }),
  );

const setting = {
  groupId: "CG-EMPLOYMENT-STATUS",
  codeValue: "ACTIVE",
  useYn: "Y",
  effectiveStartDate: "2026-09-22",
  effectiveEndDate: null,
};

describe("DetailCodeUsagePage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("saves a selected detail code usage setting only after confirmation and requeries the last criteria", async () => {
    const savedSetting = { ...setting, useYn: "N" };
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() => response([setting]))
      .mockImplementationOnce(() => response(savedSetting))
      .mockImplementationOnce(() => response([savedSetting]));
    vi.stubGlobal("fetch", fetchMock);
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );

    render(<DetailCodeUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText(setting.codeValue);

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(
      screen.getByTestId("detail-code-usage-edit-use-yn-select"),
      {
        target: { value: "N" },
      },
    );
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    await screen.findByText(
      "저장 후 코드 사용여부와 적용기간을 다시 조회했습니다.",
    );
    expect(fetchMock.mock.calls[1][0]).toBe("/api/system/common-codes/usage");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST" });
    expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toMatchObject({
      groupId: setting.groupId,
      codeValue: setting.codeValue,
      useYn: "N",
    });
    expect(fetchMock.mock.calls[2][0]).toContain("size=20");
    expect(screen.getAllByText("중지")).not.toHaveLength(0);
  });

  it("shows the permission state after a forbidden usage query", async () => {
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

    render(<DetailCodeUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText(
      "권한이 없어 코드 사용 설정을 조회하거나 변경할 수 없습니다.",
    );
    expect(
      screen.queryByRole("button", { name: "수정" }),
    ).not.toBeInTheDocument();
  });

  it("shows a loading notice while a usage query is pending", () => {
    vi.stubGlobal(
      "fetch",
      vi.fn(() => new Promise<Response>(() => undefined)),
    );

    render(<DetailCodeUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    expect(
      screen.getByText("코드 사용 설정을 조회하고 있습니다."),
    ).toBeInTheDocument();
  });

  it("shows the empty state when the current criteria have no settings", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(response([])));

    render(<DetailCodeUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("조건에 맞는 코드 사용 설정이 없습니다.");
  });

  it("shows a retry action after a non-permission query failure", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "INTERNAL_ERROR" },
            meta: {},
          }),
          { status: 500 },
        ),
      ),
    );

    render(<DetailCodeUsagePage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));

    await screen.findByText("코드 사용 설정을 조회하지 못했습니다.");
    expect(
      screen.getByRole("button", { name: "다시 시도" }),
    ).toBeInTheDocument();
  });
});
