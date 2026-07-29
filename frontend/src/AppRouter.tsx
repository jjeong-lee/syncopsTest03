import { useCallback, useEffect, useState } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { CodeGroupsPage } from "./pages/CodeGroupsPage";
import { DetailCodesPage } from "./pages/DetailCodesPage";
import { MenuInfoPage } from "./pages/MenuInfoPage";
import { MenuPermissionsPage } from "./pages/MenuPermissionsPage";
import { MenuStructurePage } from "./pages/MenuStructurePage";
import { OrganizationsPage } from "./pages/OrganizationsPage";
import { RolesPage } from "./pages/RolesPage";
import { SystemDashboardPage } from "./pages/SystemDashboardPage";
import { UserRolesPage } from "./pages/UserRolesPage";
import { UsersPage } from "./pages/UsersPage";
import { getCurrentUser, listCurrentMenus } from "./services/foundationApi";
import type { Menu } from "./types";

function ProtectedShell({
  authenticated,
  menus,
  children,
}: {
  authenticated: boolean;
  menus: Menu[];
  children: React.ReactNode;
}) {
  const location = useLocation();
  if (!authenticated)
    return (
      <Navigate
        to={`/login?redirect=${encodeURIComponent(location.pathname)}`}
        replace
      />
    );
  return <AppShell menus={menus}>{children}</AppShell>;
}

export function AppRouter() {
  const [authenticated, setAuthenticated] = useState(false);
  const [checking, setChecking] = useState(true);
  const [menus, setMenus] = useState<Menu[]>([]);

  const loadShell = useCallback(async () => {
    try {
      await getCurrentUser();
      const current = (await listCurrentMenus()) as Menu[];
      setMenus(current);
      setAuthenticated(true);
    } catch {
      setAuthenticated(false);
      setMenus([]);
    } finally {
      setChecking(false);
    }
  }, []);

  useEffect(() => {
    loadShell();
  }, [loadShell]);

  if (checking) {
    return (
      <div className="grid min-h-svh place-items-center bg-slate-50 text-sm font-medium text-slate-500">
        세션을 확인하는 중입니다...
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginPage onLogin={loadShell} />} />
      <Route path="/" element={<Navigate to="/system" replace />} />
      <Route
        path="/system"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <SystemDashboardPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/users"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <UsersPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/organizations"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <OrganizationsPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/roles"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <RolesPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/user-roles"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <UserRolesPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/menu-permissions"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <MenuPermissionsPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/menu-structure"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <MenuStructurePage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/menu-info"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <MenuInfoPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/code-groups"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <CodeGroupsPage />
          </ProtectedShell>
        }
      />
      <Route
        path="/system/code-groups/:groupId/detail-codes"
        element={
          <ProtectedShell authenticated={authenticated} menus={menus}>
            <DetailCodesPage />
          </ProtectedShell>
        }
      />
      <Route path="*" element={<Navigate to="/system" replace />} />
    </Routes>
  );
}
