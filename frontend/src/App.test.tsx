import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

const apiResponse = (data: unknown) =>
  new Response(JSON.stringify({ success: true, data, meta: {} }), {
    status: 200,
  });

describe("App menu authorization", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("removes the non-functional top navigation while retaining the authorized sidebar", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(
          apiResponse({
            menus: [
              {
                menuId: "MENU-MENU-PERMISSION-MANAGEMENT",
                menuName: "메뉴 권한 관리",
                route: "/system/roles-permissions/menu-permissions",
              },
            ],
          }),
        ),
    );

    render(<App />);

    expect(
      await screen.findByRole("complementary", { name: "시스템 관리 메뉴" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "메뉴 권한 관리" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("navigation", { name: "주요 메뉴" }),
    ).not.toBeInTheDocument();
  });

  it("renders only menus returned by the same current-user authorization response", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(
          apiResponse({
            menus: [
              {
                menuId: "MENU-MENU-PERMISSION-MANAGEMENT",
                menuName: "메뉴 권한 관리",
                route: "/system/roles-permissions/menu-permissions",
              },
            ],
          }),
        ),
    );

    render(<App />);

    await screen.findByRole("link", { name: "메뉴 권한 관리" });
    expect(
      screen.getByRole("link", { name: "메뉴 권한 관리" }),
    ).toHaveAttribute("href", "/system/roles-permissions/menu-permissions");
    expect(
      screen.queryByRole("link", { name: "사용자 관리" }),
    ).not.toBeInTheDocument();
  });

  it("shows a permission notice instead of a directly entered route when the current-user menu response excludes it", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(apiResponse({ menus: [] })),
    );

    render(<App />);

    await waitFor(() =>
      expect(
        screen.getByText("접근 권한이 있는 메뉴를 선택하세요."),
      ).toBeInTheDocument(),
    );
    expect(
      screen.queryByRole("button", { name: "조회" }),
    ).not.toBeInTheDocument();
  });

  it.each([
    ["/system/user-organization/users", "사용자 관리"],
    ["/system/user-organization/organizations", "조직 관리"],
    ["/system/roles-permissions/roles", "역할 관리"],
    ["/system/roles-permissions/user-roles", "사용자 역할 관리"],
    ["/system/roles-permissions/menu-permissions", "메뉴 권한 관리"],
    ["/system/menus/structure", "메뉴 구조 관리"],
    ["/system/menus/information", "메뉴 정보 관리"],
    ["/system/external-integrations/school-information", "학교정보 조회"],
    ["/system/common-codes/groups", "코드그룹 관리"],
    [
      "/system/common-codes/detail-codes?groupId=CG-EMPLOYMENT-STATUS",
      "상세코드 관리",
    ],
  ])(
    "renders the authorized target screen for %s without a route error",
    async (route, heading) => {
      window.history.replaceState({}, "", route);
      vi.stubGlobal(
        "fetch",
        vi
          .fn()
          .mockResolvedValueOnce(apiResponse({ status: "UP" }))
          .mockResolvedValueOnce(
            apiResponse({
              menus: [
                {
                  menuId: "MENU-TARGET",
                  menuName: heading,
                  route: window.location.pathname,
                },
              ],
            }),
          ),
      );

      render(<App />);

      expect(
        await screen.findByRole("heading", { name: heading }),
      ).toBeInTheDocument();
      expect(
        screen.queryByText("접근 권한이 있는 메뉴를 선택하세요."),
      ).not.toBeInTheDocument();
    },
  );
});

describe("로그인 진입", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("로그인 화면에 자격 증명 입력, 로그인·로그아웃 제어 및 오류 영역을 표시하고 인증 오류를 안내한다", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "INVALID_CREDENTIALS", message: "인증 실패" },
          }),
          { status: 401 },
        ),
      ),
    );

    render(<App />);

    expect(screen.getByTestId("auth-login-screen")).toHaveClass("login-page");
    expect(screen.getByLabelText("사용자 ID")).toBeInTheDocument();
    expect(screen.getByLabelText("비밀번호")).toBeInTheDocument();
    expect(screen.getByTestId("auth-login-button")).toBeInTheDocument();
    expect(screen.getByTestId("auth-login-button")).toHaveClass(
      "primary-action",
      "login-submit",
    );
    expect(screen.getByTestId("auth-logout-button")).toBeInTheDocument();
    expect(screen.getByTestId("auth-login-error")).toBeEmptyDOMElement();

    fireEvent.click(screen.getByTestId("auth-login-button"));

    expect(
      await screen.findByText("계정 정보를 확인한 뒤 다시 시도하세요."),
    ).toBeInTheDocument();
    expect(screen.queryByTestId("auth-shell")).not.toBeInTheDocument();
  });

  it("인증 서비스 오류가 발생하면 잠시 후 재시도 안내를 표시한다", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "SERVICE_UNAVAILABLE", message: "인증 서비스 오류" },
          }),
          { status: 503 },
        ),
      ),
    );

    render(<App />);
    fireEvent.click(screen.getByTestId("auth-login-button"));

    expect(
      await screen.findByText(
        "서비스에 연결할 수 없습니다. 잠시 후 다시 시도하세요.",
      ),
    ).toBeInTheDocument();
    expect(screen.queryByTestId("auth-shell")).not.toBeInTheDocument();
  });

  it("유효한 자격 증명을 로그인 API에 보내고 응답 메뉴만 사이드바에 표시한다", async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(
      apiResponse({
        userId: "admin",
        roleCodes: ["R09"],
        menus: [
          {
            menuId: "MENU-MENU-PERMISSION-MANAGEMENT",
            menuName: "메뉴 권한 관리",
            route: "/system/roles-permissions/menu-permissions",
          },
        ],
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);

    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("비밀번호"), {
      target: { value: "admin" },
    });
    fireEvent.click(screen.getByTestId("auth-login-button"));

    expect(await screen.findByTestId("auth-shell")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/auth/login",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ userId: "admin", password: "admin" }),
      }),
    );
    expect(
      screen.getByRole("link", { name: "메뉴 권한 관리" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("link", { name: "사용자 관리" }),
    ).not.toBeInTheDocument();
  });
});

describe("미인증 인증 확인", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("인증되지 않은 보호 화면 진입은 401 뒤 로그인 화면을 표시하고 denied 문구를 남기지 않는다", async () => {
    window.history.replaceState({}, "", "/system/roles-permissions/roles");
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(
          new Response(
            JSON.stringify({
              success: false,
              error: { code: "UNAUTHENTICATED", message: "인증이 필요합니다." },
            }),
            { status: 401 },
          ),
        ),
    );

    render(<App />);

    expect(await screen.findByTestId("auth-login-screen")).toBeInTheDocument();
    expect(screen.queryByTestId("auth-shell")).not.toBeInTheDocument();
    expect(
      screen.queryByText("권한이 있는 메뉴만 표시합니다."),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText("접근 권한이 있는 메뉴를 선택하세요."),
    ).not.toBeInTheDocument();
  });

  it("만료된 세션의 보호 화면 진입도 401 뒤 로그인 화면을 표시한다", async () => {
    window.history.replaceState({}, "", "/system/common-codes/groups");
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(apiResponse({ status: "UP" }))
        .mockResolvedValueOnce(
          new Response(
            JSON.stringify({
              success: false,
              error: {
                code: "SESSION_EXPIRED",
                message: "세션이 만료되었습니다.",
              },
            }),
            { status: 401 },
          ),
        ),
    );

    render(<App />);

    expect(await screen.findByTestId("auth-login-screen")).toBeInTheDocument();
    expect(screen.queryByTestId("auth-shell")).not.toBeInTheDocument();
    expect(
      screen.queryByText("권한이 있는 메뉴만 표시합니다."),
    ).not.toBeInTheDocument();
  });
});

describe("로그아웃", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("로그아웃 뒤 로그인 화면을 표시하고 빈 비밀번호 로그인 요청을 보내지 않는다", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        apiResponse({ userId: "admin", roleCodes: ["R09"], menus: [] }),
      )
      .mockResolvedValueOnce(apiResponse({ status: "UP" }))
      .mockResolvedValueOnce(apiResponse(null));
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);
    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("비밀번호"), {
      target: { value: "admin" },
    });
    fireEvent.click(screen.getByTestId("auth-login-button"));
    await screen.findByTestId("auth-shell");

    fireEvent.click(screen.getByTestId("shell-logout-button"));

    expect(await screen.findByTestId("auth-login-screen")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalledWith(
      "/api/auth/login",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ userId: "admin", password: "" }),
      }),
    );
  });

  it("기존 logout endpoint로 현재 세션 종료를 요청한 뒤 로그인 화면으로 복귀한다", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        apiResponse({ userId: "admin", roleCodes: ["R09"], menus: [] }),
      )
      .mockResolvedValueOnce(apiResponse({ status: "UP" }))
      .mockResolvedValueOnce(apiResponse(null));
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);
    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("비밀번호"), {
      target: { value: "admin" },
    });
    fireEvent.click(screen.getByTestId("auth-login-button"));
    await screen.findByTestId("auth-shell");

    fireEvent.click(screen.getByTestId("shell-logout-button"));

    await screen.findByTestId("auth-login-screen");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/auth/logout",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ userId: "admin", password: "admin" }),
      }),
    );
    expect(
      fetchMock.mock.calls.filter(([path]) => path === "/api/auth/login"),
    ).toHaveLength(1);
  });
});

describe("인증 전환 통합 회귀", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
    window.history.replaceState({}, "", "/");
  });

  it("미인증 보호 화면에서 로그인한 뒤 로그인 응답의 허용 메뉴만 사이드바에 표시한다", async () => {
    window.history.replaceState(
      {},
      "",
      "/system/roles-permissions/menu-permissions",
    );
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(apiResponse({ status: "UP" }))
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: false,
            error: { code: "UNAUTHENTICATED", message: "인증이 필요합니다." },
          }),
          { status: 401 },
        ),
      )
      .mockResolvedValueOnce(
        apiResponse({
          userId: "admin",
          roleCodes: ["R09"],
          menus: [
            {
              menuId: "MENU-MENU-PERMISSION-MANAGEMENT",
              menuName: "메뉴 권한 관리",
              route: "/system/roles-permissions/menu-permissions",
            },
          ],
        }),
      )
      .mockResolvedValueOnce(apiResponse({ status: "UP" }))
      .mockResolvedValueOnce(apiResponse([]));
    vi.stubGlobal("fetch", fetchMock);

    render(<App />);

    expect(await screen.findByTestId("auth-login-screen")).toBeInTheDocument();
    expect(
      screen.queryByText("접근 권한이 있는 메뉴를 선택하세요."),
    ).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText("사용자 ID"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("비밀번호"), {
      target: { value: "admin" },
    });
    fireEvent.click(screen.getByTestId("auth-login-button"));

    expect(await screen.findByTestId("auth-shell")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/auth/login",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ userId: "admin", password: "admin" }),
      }),
    );
    expect(
      screen.getByRole("link", { name: "메뉴 권한 관리" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("link", { name: "사용자 관리" }),
    ).not.toBeInTheDocument();
  });
});
