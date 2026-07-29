import {
  Activity,
  Building2,
  ChevronRight,
  Code2,
  KeyRound,
  LayoutDashboard,
  MenuSquare,
  Search,
  ShieldCheck,
  UsersRound,
} from "lucide-react";
import type { ReactNode } from "react";
import { NavLink, useLocation } from "react-router-dom";
import { getDefaultDetailCodeRoute, navGroups } from "../config";
import type { Menu, ScreenConfig } from "../types";
import { Badge } from "./ui";

const iconMap: Record<string, typeof LayoutDashboard> = {
  "SYSTEM-SMOKE-DASHBOARD": LayoutDashboard,
  "CMN-USER-MGMT": UsersRound,
  "CMN-ORG-MGMT": Building2,
  "CMN-ROLE-MGMT": ShieldCheck,
  "CMN-USER-ROLE-MGMT": KeyRound,
  "CMN-MENU-AUTH-MGMT": ShieldCheck,
  "CMN-MENU-STRUCT-MGMT": MenuSquare,
  "CMN-MENU-INFO-MGMT": MenuSquare,
  "CMN-CODE-GROUP-MGMT": Code2,
  "CMN-DETAIL-CODE-MGMT": Code2,
};

function routeFor(screen: ScreenConfig) {
  return screen.route.includes(":groupId")
    ? getDefaultDetailCodeRoute()
    : screen.route;
}

function isAllowed(screen: ScreenConfig, menus: Menu[]) {
  return (
    screen.route === "/system" ||
    menus.some((menu) => menu.screenId === screen.id)
  );
}

export function AppShell({
  menus,
  children,
}: {
  menus: Menu[];
  children: ReactNode;
}) {
  const location = useLocation();
  const allowedGroups = navGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => isAllowed(item, menus)),
    }))
    .filter((group) => group.items.length > 0);

  return (
    <div className="min-h-svh bg-slate-50 text-slate-950 lg:grid lg:grid-cols-[18rem_1fr]">
      <aside className="border-r border-slate-200 bg-white lg:sticky lg:top-0 lg:h-svh">
        <div className="flex h-16 items-center gap-3 border-b border-slate-200 px-4">
          <div className="grid size-9 place-items-center rounded-lg bg-slate-950 text-white shadow-sm">
            <Activity className="size-4" />
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold leading-none">
              FPE 공통관리
            </p>
            <p className="mt-1 truncate text-xs text-slate-500">
              R09 system console
            </p>
          </div>
        </div>
        <nav className="grid gap-4 p-3" aria-label="시스템 관리 메뉴">
          {allowedGroups.map((group) => {
            const groupActive = group.items.some((screen) => {
              const target = routeFor(screen);
              return (
                location.pathname === target ||
                (screen.route.includes(":groupId") &&
                  location.pathname.startsWith("/system/code-groups/") &&
                  location.pathname.endsWith("/detail-codes"))
              );
            });
            return (
              <section key={group.label} className="grid gap-1">
                <div className="flex items-center gap-2 px-2 py-1.5 text-xs font-semibold uppercase tracking-wide text-slate-500">
                  <ChevronRight
                    className={`size-3 transition-transform duration-200 ${groupActive ? "rotate-90" : ""}`}
                  />
                  {group.label}
                </div>
                <div className="grid gap-1">
                  {group.items.map((screen) => {
                    const Icon = iconMap[screen.id] ?? LayoutDashboard;
                    const target = routeFor(screen);
                    return (
                      <NavLink
                        key={screen.id}
                        to={target}
                        end={screen.route === "/system"}
                        className={({ isActive }) => {
                          const dynamicActive =
                            screen.route.includes(":groupId") &&
                            location.pathname.startsWith(
                              "/system/code-groups/",
                            ) &&
                            location.pathname.endsWith("/detail-codes");
                          return `group flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-300 ${isActive || dynamicActive ? "bg-slate-950 text-white shadow-sm" : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"}`;
                        }}
                      >
                        <Icon className="size-4 shrink-0" />
                        <span className="truncate">{screen.title}</span>
                      </NavLink>
                    );
                  })}
                </div>
              </section>
            );
          })}
        </nav>
      </aside>
      <div className="min-w-0">
        <header className="sticky top-0 z-30 flex h-16 items-center gap-4 border-b border-slate-200 bg-white/95 px-4 backdrop-blur supports-[backdrop-filter]:bg-white/80 sm:px-6">
          <div className="hidden items-center gap-2 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-500 shadow-sm md:flex">
            <Search className="size-4" />
            메뉴, 화면ID, route 검색
          </div>
          <div className="ml-auto flex items-center gap-2">
            <Badge tone="success">R09</Badge>
            <Badge tone="muted">React Router</Badge>
          </div>
        </header>
        <main className="px-4 py-6 sm:px-6 lg:px-8">{children}</main>
      </div>
    </div>
  );
}
