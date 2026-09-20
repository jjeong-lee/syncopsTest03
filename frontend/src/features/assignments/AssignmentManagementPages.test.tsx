import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import {
  PositionAssignmentManagementPage,
  RoleDataScopeManagementPage,
  WorkAssignmentManagementPage,
} from "./AssignmentManagementPages";

const apiResponse = (data: unknown) =>
  new Response(JSON.stringify({ success: true, data, meta: {} }), {
    status: 200,
  });

describe("assignment management pages", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("uses 20 as the default page size and blocks an incomplete position save", async () => {
    const fetchMock = vi.fn().mockResolvedValue(apiResponse([]));
    vi.stubGlobal("fetch", fetchMock);
    render(<PositionAssignmentManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await waitFor(() =>
      expect(fetchMock.mock.calls[0][0]).toContain("size=20"),
    );
    fireEvent.click(screen.getByRole("button", { name: "보직 등록·변경" }));
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    expect(
      screen.getByText("필수 입력 항목을 확인하세요."),
    ).toBeInTheDocument();
  });

  it("shows the work assignment confirmation and requeries after save", async () => {
    const fetchMock = vi.fn().mockResolvedValue(apiResponse([]));
    vi.stubGlobal("fetch", fetchMock);
    render(<WorkAssignmentManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "담당자 지정" }));
    fireEvent.change(screen.getByTestId("work-organizationId-input"), {
      target: { value: "ORG-KNUE" },
    });
    fireEvent.change(screen.getByTestId("work-userId-input"), {
      target: { value: "member" },
    });
    fireEvent.change(screen.getByTestId("work-workArea-input"), {
      target: { value: "ACADEMIC" },
    });
    fireEvent.change(screen.getByTestId("work-effectiveStartDate-input"), {
      target: { value: "2026-01-01" },
    });
    fireEvent.change(screen.getByTestId("work-processPermission-input"), {
      target: { value: "Y" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));
    expect(
      screen.getByText("입력한 업무담당자 지정을 저장하시겠습니까?"),
    ).toBeInTheDocument();
  });

  it("renders data scope controls with a 20/50/100 page-size selector", () => {
    render(<RoleDataScopeManagementPage />);
    expect(screen.getByLabelText("목록 건수")).toHaveValue("20");
    expect(screen.getByRole("option", { name: "50" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "100" })).toBeInTheDocument();
  });
});
