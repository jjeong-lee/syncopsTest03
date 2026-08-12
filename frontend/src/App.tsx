import { type FormEvent, useEffect, useState } from "react";
import { BrowserRouter, Link, NavLink, useLocation } from "react-router-dom";
import { AppRouter } from "./AppRouter";
import { ApiRequestError, apiRequest } from "./shared/api/client";
import "./styles.css";

type HealthData = { status: "UP" };
type AuthorizedMenu = {
  menuId: string;
  menuName: string;
  route: string | null;
};
type CurrentUser = { menus: AuthorizedMenu[] };
type LoginCredentials = { userId: string; password: string };

type LoginScreenProps = {
  onAuthenticated: (
    credentials: LoginCredentials,
    menus: AuthorizedMenu[],
  ) => void;
};

const menuGroups = [
  { label: "사용자·조직 관리", prefix: "/system/user-organization/" },
  { label: "역할·권한 관리", prefix: "/system/roles-permissions/" },
  { label: "메뉴 관리", prefix: "/system/menus/" },
  { label: "공통코드 관리", prefix: "/system/common-codes/" },
  { label: "보안·감사 관리", prefix: "/system/security-audit/" },
];

function LoginScreen({ onAuthenticated }: LoginScreenProps) {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response = await apiRequest<CurrentUser>("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, password }),
      });
      onAuthenticated({ userId, password }, response.data.menus);
    } catch (error) {
      setErrorMessage(
        error instanceof ApiRequestError && error.status === 401
          ? "계정 정보를 확인한 뒤 다시 시도하세요."
          : "서비스에 연결할 수 없습니다. 잠시 후 다시 시도하세요.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="login-page" data-testid="auth-login-screen">
      <section className="login-panel" aria-labelledby="login-title">
        <p className="login-kicker">FACULTY ASSESSMENT</p>
        <h1 id="login-title">교수업적평가 시스템</h1>
        <p className="login-copy">
          시스템 관리 업무를 계속하려면 로그인하세요.
        </p>
        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="login-user-id">사용자 ID</label>
          <input
            id="login-user-id"
            data-testid="auth-user-id-input"
            autoComplete="username"
            value={userId}
            onChange={(event) => setUserId(event.target.value)}
          />
          <label htmlFor="login-password">비밀번호</label>
          <input
            id="login-password"
            data-testid="auth-password-input"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <p
            className="login-error"
            data-testid="auth-login-error"
            aria-live="polite"
          >
            {errorMessage}
          </p>
          <button
            className="primary-action login-submit"
            data-testid="auth-login-button"
            disabled={isSubmitting}
            type="submit"
          >
            {isSubmitting ? "로그인 중" : "로그인"}
          </button>
          <button
            className="login-logout-button"
            data-testid="auth-logout-button"
            disabled
            type="button"
          >
            로그 아웃
          </button>
        </form>
      </section>
    </main>
  );
}

function SystemShell({
  initialMenus,
  onUnauthenticated,
  onLogout,
}: {
  initialMenus: AuthorizedMenu[] | null;
  onUnauthenticated: () => void;
  onLogout: () => Promise<void>;
}) {
  const location = useLocation();
  const [status, setStatus] = useState<"loading" | "available" | "error">(
    "loading",
  );
  const [authorizedMenus, setAuthorizedMenus] = useState<AuthorizedMenu[]>(
    initialMenus ?? [],
  );
  const [menuState, setMenuState] = useState<"loading" | "ready" | "denied">(
    initialMenus ? "ready" : "loading",
  );
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  async function handleLogout() {
    setIsLoggingOut(true);
    try {
      await onLogout();
    } finally {
      setIsLoggingOut(false);
    }
  }

  useEffect(() => {
    void apiRequest<HealthData>("/api/health")
      .then((response) =>
        setStatus(response.data.status === "UP" ? "available" : "error"),
      )
      .catch(() => setStatus("error"));
  }, []);

  useEffect(() => {
    if (initialMenus) return;

    void apiRequest<CurrentUser>("/api/auth/me")
      .then((response) => {
        setAuthorizedMenus(response.data.menus);
        setMenuState("ready");
      })
      .catch((error) => {
        if (error instanceof ApiRequestError && error.status === 401) {
          onUnauthenticated();
          return;
        }
        setMenuState("denied");
      });
  }, [initialMenus, onUnauthenticated]);

  const permittedRoutes = authorizedMenus.flatMap((menu) =>
    menu.route ? [menu.route] : [],
  );
  const isSystemRoute = location.pathname.startsWith("/system/");
  const showSystemShell = isSystemRoute || initialMenus !== null;

  return (
    <div className="app-shell" data-testid="auth-shell">
      <a
        className="skip-link"
        href="#main-content"
        data-testid="skip-to-main-link"
      >
        본문으로 바로가기
      </a>
      <header className="site-header">
        <div className="header-top">
          <div className="content-rail header-top-content">
            <Link
              className="brand"
              to="/"
              aria-label="교수업적평가 시스템 홈"
              data-testid="home-link"
            >
              교수업적평가 <span>시스템</span>
            </Link>
            <div className="header-search" role="search" aria-label="통합 검색">
              <span>시스템 관리</span>
              <input
                aria-label="메뉴 검색"
                placeholder="메뉴를 찾아보세요"
                data-testid="menu-search-input"
              />
              <button
                type="button"
                aria-label="검색"
                data-testid="menu-search-button"
              >
                ⌕
              </button>
            </div>
            <div className="header-utility">
              <p className="utility-copy">
                한국교원대학교 ·{" "}
                {status === "available"
                  ? "연결됨"
                  : status === "loading"
                    ? "연결 확인 중"
                    : "연결 확인 필요"}
              </p>
              <button
                className="shell-logout-button"
                data-testid="shell-logout-button"
                disabled={isLoggingOut}
                onClick={() => void handleLogout()}
                type="button"
              >
                {isLoggingOut ? "로그아웃 중" : "로그아웃"}
              </button>
            </div>
          </div>
        </div>
        <nav className="header-navigation" aria-label="주요 메뉴">
          <div className="content-rail navigation-content">
            <span className="all-menu-mark" aria-hidden="true">
              ☰
            </span>
            <span className="menu-label">전체 메뉴</span>
            <span className="menu-description">교수업적평가 시스템 관리</span>
          </div>
        </nav>
      </header>
      <div className={showSystemShell ? "system-layout" : "content-rail"}>
        {showSystemShell && (
          <aside className="system-sidebar" aria-label="시스템 관리 메뉴">
            <p className="sidebar-kicker">SYSTEM</p>
            <h2>시스템 관리</h2>
            {menuState === "loading" ? (
              <div className="sidebar-skeleton" />
            ) : (
              menuGroups.map((group) => {
                const menus = authorizedMenus.filter((menu) =>
                  menu.route?.startsWith(group.prefix),
                );
                if (menus.length === 0) return null;
                return (
                  <section className="sidebar-group" key={group.prefix}>
                    <h3>{group.label}</h3>
                    <ul>
                      {menus.map((menu) => (
                        <li
                          key={menu.menuId}
                          data-testid={`authorized-menu-${menu.menuId}`}
                        >
                          <NavLink
                            to={menu.route!}
                            className={({ isActive }) =>
                              isActive ? "active" : ""
                            }
                            data-testid={`authorized-menu-link-${menu.menuId}`}
                          >
                            {menu.menuName}
                          </NavLink>
                        </li>
                      ))}
                    </ul>
                  </section>
                );
              })
            )}
            {menuState === "denied" && (
              <p className="sidebar-note">권한이 있는 메뉴만 표시합니다.</p>
            )}
          </aside>
        )}
        <main
          className={
            showSystemShell
              ? "foundation-content system-content"
              : "foundation-content"
          }
          id="main-content"
        >
          <AppRouter
            isReady={menuState !== "loading"}
            permittedRoutes={permittedRoutes}
          />
        </main>
      </div>
    </div>
  );
}

function Application() {
  const location = useLocation();
  const [loginMenus, setLoginMenus] = useState<AuthorizedMenu[] | null>(null);
  const [logoutCredentials, setLogoutCredentials] =
    useState<LoginCredentials | null>(null);
  const [showLogin, setShowLogin] = useState(location.pathname === "/");

  function handleUnauthenticated() {
    setLoginMenus(null);
    setLogoutCredentials(null);
    setShowLogin(true);
  }

  async function handleLogout() {
    if (!logoutCredentials) {
      handleUnauthenticated();
      return;
    }

    await apiRequest<void>("/api/auth/logout", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(logoutCredentials),
    });
    handleUnauthenticated();
  }

  if (showLogin) {
    return (
      <LoginScreen
        onAuthenticated={(credentials, menus) => {
          setLogoutCredentials(credentials);
          setLoginMenus(menus);
          setShowLogin(false);
        }}
      />
    );
  }

  return (
    <SystemShell
      initialMenus={loginMenus}
      onUnauthenticated={handleUnauthenticated}
      onLogout={handleLogout}
    />
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Application />
    </BrowserRouter>
  );
}
