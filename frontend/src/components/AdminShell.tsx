import { NavLink, Outlet, useLocation } from "react-router-dom";
import { adminRoutes } from "../app/routes";

const groupDescriptions: Record<string, string> = {
  "사용자·조직 관리": "KORUS 기준정보",
  "역할·권한 관리": "R01-R09 접근 제어",
  "메뉴 관리": "route와 화면 연결",
  "공통코드 관리": "group/detail 기준값",
};

export function AdminShell({ children }: { children?: React.ReactNode }) {
  const location = useLocation();
  const groups = adminRoutes.reduce<Record<string, typeof adminRoutes>>(
    (acc, route) => {
      acc[route.group] = [...(acc[route.group] ?? []), route];
      return acc;
    },
    {},
  );
  return (
    <div className="min-h-svh bg-sidebar text-foreground lg:grid lg:grid-cols-[18rem_minmax(0,1fr)]">
      <aside
        className="sticky top-0 z-40 flex max-h-svh flex-col gap-4 border-r border-sidebar-border bg-sidebar p-3 text-sidebar-foreground max-lg:static max-lg:max-h-none"
        aria-label="시스템 관리 메뉴"
      >
        <div className="flex items-center gap-3 rounded-lg px-2 py-2">
          <span className="grid size-9 place-items-center rounded-lg bg-sidebar-primary text-sm font-bold text-sidebar-primary-foreground shadow-sm">
            KN
          </span>
          <span className="grid leading-tight">
            <span className="text-sm font-semibold">교수업적평가</span>
            <span className="text-xs text-sidebar-foreground/65">
              공통기능 1차
            </span>
          </span>
        </div>
        <div className="grid gap-3 overflow-y-auto pr-1">
          {Object.entries(groups).map(([group, routes]) => (
            <nav key={group} className="grid gap-1" aria-label={group}>
              <div className="px-2 py-1">
                <strong className="block text-xs font-medium text-sidebar-foreground/70">
                  {group}
                </strong>
                <span className="text-[11px] text-sidebar-foreground/45">
                  {groupDescriptions[group]}
                </span>
              </div>
              {routes.map((route) => (
                <NavLink
                  key={route.path}
                  className={({ isActive }) =>
                    `flex items-center justify-between rounded-md px-3 py-2 text-sm transition-all duration-200 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring ${
                      isActive
                        ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground shadow-sm"
                        : "text-sidebar-foreground/80"
                    }`
                  }
                  to={route.path}
                >
                  <span>{route.title}</span>
                  {route.path === location.pathname && (
                    <span className="size-1.5 rounded-full bg-sidebar-primary" />
                  )}
                </NavLink>
              ))}
            </nav>
          ))}
        </div>
      </aside>
      <main className="m-2 min-h-[calc(100svh-1rem)] rounded-xl bg-background shadow-sm">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-3 rounded-t-xl border-b bg-background/95 px-4 backdrop-blur-sm">
          <span className="text-sm text-muted-foreground">
            공통기능 1차 관리자 검증
          </span>
          <strong className="rounded-md border bg-card px-3 py-1 text-sm font-medium">
            R09 시스템관리자
          </strong>
        </header>
        <div className="px-4 py-6 lg:px-6">{children ?? <Outlet />}</div>
      </main>
    </div>
  );
}
