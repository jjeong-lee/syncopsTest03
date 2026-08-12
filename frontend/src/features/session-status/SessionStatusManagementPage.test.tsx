import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { SessionStatusManagementPage } from "./SessionStatusManagementPage";

describe("SessionStatusManagementPage", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads active sessions and submits the selected session termination reason", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: true,
            data: [
              {
                sessionId: "session-1",
                userId: "member",
                loginAt: "2026-08-12T00:00:00Z",
                lastActivityAt: "2026-08-12T01:00:00Z",
                ipAddress: "203.0.113.8",
                status: "ACTIVE",
              },
            ],
            meta: {},
          }),
          { status: 200 },
        ),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ success: true, data: null, meta: {} }), {
          status: 200,
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ success: true, data: [], meta: {} }), {
          status: 200,
        }),
      );
    vi.stubGlobal("fetch", fetchMock);
    render(<SessionStatusManagementPage />);

    fireEvent.click(screen.getByTestId("session-status-search-button"));
    await screen.findByTestId("session-status-detail-session-1");
    fireEvent.click(screen.getByTestId("session-status-detail-session-1"));
    fireEvent.change(screen.getByTestId("session-termination-reason-input"), {
      target: { value: "보안 점검" },
    });
    fireEvent.click(screen.getByTestId("session-termination-confirm-button"));

    await screen.findByText("현재 활성 세션이 없습니다.");
    expect(fetchMock.mock.calls[1][0]).toBe(
      "/api/session-status/session-1/termination",
    );
    expect(fetchMock.mock.calls[1][1].body).toBe(
      JSON.stringify({ reason: "보안 점검" }),
    );
  });
});
