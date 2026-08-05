import { LogOut, Search, Settings } from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";
import { api } from "../../api/client";
import { screens } from "../screenConfigs";
import { Session } from "../types";

type Props = {
  session: Session;
  onLogout: () => void;
  children: React.ReactNode;
};

const groups = [
  {
    name: "사용자·조직 관리",
    routes: ["/system/users", "/system/organizations"],
  },
  {
    name: "역할·권한 관리",
    routes: ["/system/roles", "/system/user-roles", "/system/menu-permissions"],
  },
  {
    name: "메뉴 관리",
    routes: ["/system/menu-structure", "/system/menu-info"],
  },
  {
    name: "공통코드 관리",
    routes: ["/system/code-groups", "/system/code-details"],
  },
];

export function AppShell({ session, onLogout, children }: Props) {
  const navigate = useNavigate();
  const granted = new Set(session.menus.map((menu) => menu.routePath));
  const logout = () => {
    api("/api/auth/logout", { method: "POST" }).finally(() => {
      onLogout();
      navigate("/login", { replace: true });
    });
  };

  return (
    <div className="min-h-screen bg-white text-plumBlack antialiased">
      <a className="sr-only focus:not-sr-only" href="#main">
        본문으로 이동
      </a>
      <header className="fixed left-0 right-0 top-0 z-[671] flex h-20 items-center gap-2 bg-white px-4 transition-[left] duration-200 ease-out">
        <button
          className="flex h-12 w-12 items-center justify-center rounded-full text-2xl font-black text-pinterest transition-colors hover:bg-black/5"
          onClick={() => navigate("/system/users")}
          aria-label="홈"
        >
          F
        </button>
        <div className="hidden h-12 min-w-[60px] items-center rounded-full bg-black px-4 font-semibold text-white lg:flex">
          관리
        </div>
        <div className="flex h-12 min-w-0 flex-1 items-center gap-2 rounded-3xl bg-warmSand px-4 focus-within:ring-2 focus-within:ring-[#435ee5] focus-within:ring-offset-2">
          <Search className="h-5 w-5 text-oliveMuted" />
          <input
            className="w-full bg-transparent text-base outline-none placeholder:text-oliveMuted"
            placeholder="메뉴, 사용자, 코드 검색"
          />
        </div>
        <span className="hidden text-sm font-semibold md:inline">
          {session.displayName}
        </span>
        <button
          className="hidden h-12 w-12 rounded-full transition-colors hover:bg-black/5 md:block"
          aria-label="설정"
        >
          <Settings className="m-auto h-5 w-5" />
        </button>
        <button className="pin-button-secondary h-10" onClick={logout}>
          <LogOut className="mr-1 h-4 w-4" />
          로그아웃
        </button>
      </header>
      <div className="flex pt-20">
        <aside className="fixed bottom-0 left-0 top-20 hidden w-72 overflow-y-auto border-r border-[#e5e5e0] bg-white p-4 lg:block">
          <p className="px-3 text-xs font-black uppercase tracking-[0.18em] text-oliveMuted">
            System management
          </p>
          <nav className="mt-4 space-y-5" aria-label="사이드바 메뉴">
            {groups.map((group) => {
              const children = screens.filter(
                (screen) =>
                  group.routes.includes(screen.route) &&
                  granted.has(screen.route),
              );
              if (!children.length) return null;
              return (
                <section key={group.name}>
                  <h2 className="px-3 text-sm font-bold text-oliveMuted">
                    {group.name}
                  </h2>
                  <div className="mt-2 space-y-1">
                    {children.map((screen) => (
                      <NavLink
                        key={screen.id}
                        to={screen.route}
                        className={({ isActive }) =>
                          `block rounded-2xl px-4 py-3 text-sm font-bold transition-colors duration-150 ${isActive ? "bg-plumBlack text-white" : "hover:bg-warmSand"}`
                        }
                      >
                        {screen.title}
                      </NavLink>
                    ))}
                  </div>
                </section>
              );
            })}
          </nav>
        </aside>
        <main
          id="main"
          className="mx-auto w-full max-w-[1280px] px-4 pb-12 pt-6 lg:ml-72 lg:px-8"
        >
          <section
            className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3"
            aria-label="허용 메뉴 카드"
          >
            {screens
              .filter((screen) => granted.has(screen.route))
              .slice(0, 9)
              .map((screen) => (
                <NavLink
                  key={screen.id}
                  to={screen.route}
                  className={({ isActive }) =>
                    `group block min-h-[128px] overflow-hidden rounded-[32px] p-5 transition duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#435ee5] focus-visible:ring-offset-2 ${isActive ? "bg-pinterest text-white" : "bg-[#f6f6f3] hover:brightness-95"}`
                  }
                >
                  <span className="text-xs font-semibold opacity-75">
                    {screen.menuPath}
                  </span>
                  <strong className="mt-3 block text-xl font-black tracking-[-0.03em]">
                    {screen.title}
                  </strong>
                </NavLink>
              ))}
          </section>
          {children}
        </main>
      </div>
    </div>
  );
}
