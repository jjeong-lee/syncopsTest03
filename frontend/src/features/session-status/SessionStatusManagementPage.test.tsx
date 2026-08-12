import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { SessionStatusManagementPage } from "./SessionStatusManagementPage";

const response = (data: unknown) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), {
      status: 200,
    }),
  );

describe("SessionStatusManagementPage", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads active sessions, shows detail modal, and submits an R09 force-termination reason in a confirmation modal", async () => {
    const fetchMock = vi
      .fn()
      .mockImplementationOnce(() =>
        response([
          {
            sessionId: "session-member",
            userId: "member",
            loginAt: "2026-08-12T01:00:00Z",
            lastActivityAt: "2026-08-12T01:10:00Z",
            ipAddress: "203.0.113.10",
            status: "ACTIVE",
          },
        ]),
      )
      .mockImplementationOnce(() => response(null));
    vi.stubGlobal("fetch", fetchMock);

    render(<SessionStatusManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "현재 접속현황 조회" }));
    expect(await screen.findByText("member")).toBeInTheDocument();
    fireEvent.click(
      screen.getByRole("button", { name: "member 세션 상세 열기" }),
    );
    expect(
      screen.getByRole("dialog", { name: "세션 상세정보" }),
    ).toHaveTextContent("203.0.113.10");
    fireEvent.click(screen.getByRole("button", { name: "강제종료" }));
    fireEvent.change(screen.getByLabelText("강제종료 사유"), {
      target: { value: "보안 점검" },
    });
    fireEvent.click(screen.getByRole("button", { name: "강제종료 확인" }));

    await screen.findByText("세션을 강제종료했습니다.");
    expect(fetchMock).toHaveBeenLastCalledWith(
      "/api/session-status/session-member/force-terminate",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ reason: "보안 점검" }),
      }),
    );
  });
});
