import { fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MenuInformationManagementPage } from "./MenuInformationManagementPage";

const menu = {
  menuId: "MENU-MENU-INFORMATION-MANAGEMENT",
  menuName: "메뉴 정보 관리",
  parentMenuId: "MENU-MANAGEMENT",
  displayOrder: 2,
  screenId: "SCR-MENU-INFORMATION-MANAGEMENT",
  url: "/system/menus/information",
  icon: null,
  businessCategory: "SYSTEM",
  description: null,
  useYn: "Y",
};

const response = (data: unknown) =>
  Promise.resolve(
    new Response(JSON.stringify({ success: true, data, meta: {} }), {
      status: 200,
    }),
  );

describe("필수 입력 저장 차단", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("필수 화면ID가 비어 있으면 메뉴 저장 요청을 보내지 않고 필드 오류를 표시한다", async () => {
    const fetchMock = vi.fn().mockImplementation(() => response([menu]));
    vi.stubGlobal("fetch", fetchMock);

    render(<MenuInformationManagementPage />);
    fireEvent.click(screen.getByRole("button", { name: "조회" }));
    await screen.findByText("SCR-MENU-INFORMATION-MANAGEMENT");

    fireEvent.click(screen.getByRole("button", { name: "수정" }));
    fireEvent.change(screen.getByLabelText("화면ID"), {
      target: { value: "" },
    });
    fireEvent.click(screen.getByRole("button", { name: "저장" }));

    expect(screen.getByText("화면ID는 필수입니다.")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});
