import { useEffect, useState } from "react";
import { BrowserRouter, Link, NavLink, useLocation } from "react-router-dom";
import { AppRouter } from "./AppRouter";
import { apiRequest } from "./shared/api/client";
import "./styles.css";

type HealthData = { status: "UP" };
type AuthorizedMenu = {
  menuId: string;
  menuName: string;
  route: string | null;
};
type CurrentUser = { menus: AuthorizedMenu[] };

const menuGroups = [
  { label: "사용자·조직 관리", prefix: "/system/user-organization/" },
  { label: "역할·권한 관리", prefix: "/system/roles-permissions/" },
  { label: "메뉴 관리", prefix: "/system/menus/" },
  { label: "공통코드 관리", prefix: "/system/common-codes/" },
];

function SystemShell() {
  const location = useLocation();
  const [status, setStatus] = useState<"loading" | "available" | "error">(
    "loading",
  );
  const [authorizedMenus, setAuthorizedMenus] = useState<AuthorizedMenu[]>([]);
  const [menuState, setMenuState] = useState<"loading" | "ready" | "denied">(
    "loading",
  );

  useEffect(() => {
    void apiRequest<HealthData>("/api/health")
      .then((response) =>
        setStatus(response.data.status === "UP" ? "available" : "error"),
      )
      .catch(() => setStatus("error"));
    void apiRequest<CurrentUser>("/api/auth/me")
      .then((response) => {
        setAuthorizedMenus(response.data.menus);
        setMenuState("ready");
      })
      .catch(() => setMenuState("denied"));
  }, []);

  const permittedRoutes = authorizedMenus.flatMap((menu) =>
    menu.route ? [menu.route] : [],
  );
  const isSystemRoute = location.pathname.startsWith("/system/");

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        본문으로 바로가기
      </a>
      <header className="site-header">
        <div className="header-top">
          <div className="content-rail header-top-content">
            <Link className="brand" to="/" aria-label="교수업적평가 시스템 홈">
              교수업적평가 <span>시스템</span>
            </Link>
            <div className="header-search" role="search" aria-label="통합 검색">
              <span>시스템 관리</span>
              <input aria-label="메뉴 검색" placeholder="메뉴를 찾아보세요" />
              <button type="button" aria-label="검색">
                ⌕
              </button>
            </div>
            <p className="utility-copy">
              한국교원대학교 ·{" "}
              {status === "available"
                ? "연결됨"
                : status === "loading"
                  ? "연결 확인 중"
                  : "연결 확인 필요"}
            </p>
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
      <div className={isSystemRoute ? "system-layout" : "content-rail"}>
        {isSystemRoute && (
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
                        <li key={menu.menuId}>
                          <NavLink
                            to={menu.route!}
                            className={({ isActive }) =>
                              isActive ? "active" : ""
                            }
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
            isSystemRoute
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

export default function App() {
  return (
    <BrowserRouter>
      <SystemShell />
    </BrowserRouter>
  );
}
