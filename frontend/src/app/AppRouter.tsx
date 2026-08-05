import React from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { api } from "../api/client";
import { screens, firstProtectedRoute } from "./screenConfigs";
import { AppShell } from "./components/AppShell";
import { StateBlock } from "./components/StatusBlocks";
import { LoginPage } from "./pages/LoginPage";
import { UserManagementPage } from "./pages/UserManagementPage";
import { OrganizationManagementPage } from "./pages/OrganizationManagementPage";
import { RoleManagementPage } from "./pages/RoleManagementPage";
import { UserRoleManagementPage } from "./pages/UserRoleManagementPage";
import { MenuPermissionPage } from "./pages/MenuPermissionPage";
import { MenuStructurePage } from "./pages/MenuStructurePage";
import { MenuInfoPage } from "./pages/MenuInfoPage";
import { CodeGroupPage } from "./pages/CodeGroupPage";
import { CodeDetailPage } from "./pages/CodeDetailPage";
import { Session } from "./types";

const firstAllowedRoute = (session: Session | null) =>
  session?.menus.find((menu) => menu.routePath)?.routePath ??
  firstProtectedRoute;

const pageByRoute: Record<
  string,
  React.ComponentType<{ readonly: boolean }>
> = {
  "/system/users": UserManagementPage,
  "/system/organizations": OrganizationManagementPage,
  "/system/roles": RoleManagementPage,
  "/system/user-roles": UserRoleManagementPage,
  "/system/menu-permissions": MenuPermissionPage,
  "/system/menu-structure": MenuStructurePage,
  "/system/menu-info": MenuInfoPage,
  "/system/code-groups": CodeGroupPage,
  "/system/code-details": CodeDetailPage,
};

function ProtectedPage({
  session,
  readonly,
  route,
  onLogout,
}: {
  session: Session | null;
  readonly: boolean;
  route: string;
  onLogout: () => void;
}) {
  if (!session) return <Navigate to="/login" replace />;
  const granted = new Set(session.menus.map((menu) => menu.routePath));
  if (!granted.has(route)) {
    return (
      <AppShell session={session} onLogout={onLogout}>
        <StateBlock
          tone="permission"
          label="권한 없음"
          detail="현재 세션의 메뉴 권한으로 접근할 수 없는 화면입니다. 직접 URL 접근도 서버 권한과 동일하게 제한됩니다."
        />
      </AppShell>
    );
  }
  const Page = pageByRoute[route];
  return (
    <AppShell session={session} onLogout={onLogout}>
      <Page readonly={readonly} />
    </AppShell>
  );
}

export function AppRouter() {
  const [session, setSession] = React.useState<Session | null>(null);
  const [booting, setBooting] = React.useState(true);

  React.useEffect(() => {
    api<Session>("/api/auth/me")
      .then(setSession)
      .catch(() => undefined)
      .finally(() => setBooting(false));
  }, []);

  if (booting) {
    return (
      <div className="min-h-screen p-4 pt-24">
        <StateBlock
          label="세션 확인 중"
          detail="현재 로그인 상태와 허용 메뉴를 조회하고 있습니다."
        />
      </div>
    );
  }

  const readonly = !session?.roleCodes.includes("R09");

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <Navigate
              to={session ? firstAllowedRoute(session) : "/login"}
              replace
            />
          }
        />
        <Route
          path="/login"
          element={<LoginPage session={session} onLogin={setSession} />}
        />
        {screens.map((screen) => (
          <Route
            key={screen.id}
            path={screen.route}
            element={
              <ProtectedPage
                session={session}
                readonly={readonly}
                route={screen.route}
                onLogout={() => setSession(null)}
              />
            }
          />
        ))}
        <Route
          path="*"
          element={
            <Navigate
              to={session ? firstAllowedRoute(session) : "/login"}
              replace
            />
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
