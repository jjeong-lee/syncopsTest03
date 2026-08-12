import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { CodeGroupManagementPage } from "./features/code-groups/CodeGroupManagementPage";
import { DetailCodeManagementPage } from "./features/detail-codes/DetailCodeManagementPage";
import { MenuPermissionManagementPage } from "./features/menu-permissions/MenuPermissionManagementPage";
import { MenuInformationManagementPage } from "./features/menus/MenuInformationManagementPage";
import { MenuStructureManagementPage } from "./features/menus/MenuStructureManagementPage";
import { OrganizationManagementPage } from "./features/organizations/OrganizationManagementPage";
import { RoleManagementPage } from "./features/roles/RoleManagementPage";
import { UserRoleManagementPage } from "./features/user-roles/UserRoleManagementPage";
import { UserManagementPage } from "./features/users/UserManagementPage";
import { SessionStatusManagementPage } from "./features/session-status/SessionStatusManagementPage";

type AppRouterProps = {
  isReady: boolean;
  permittedRoutes: string[];
};

function PermissionNotice() {
  return (
    <section className="route-permission-state" aria-live="polite">
      <p className="state-kicker">ACCESS CHECK</p>
      <h1>권한이 없습니다.</h1>
      <p>접근 권한이 있는 메뉴를 선택하세요.</p>
    </section>
  );
}

function ProtectedRoute({
  isReady,
  permittedRoutes,
  path,
  children,
}: AppRouterProps & { path: string; children: ReactNode }) {
  if (isReady && !permittedRoutes.includes(path)) {
    return <PermissionNotice />;
  }
  return <>{children}</>;
}

function Home() {
  return (
    <section className="system-home">
      <p className="state-kicker">SYSTEM MANAGEMENT</p>
      <h1>업무 기준을 관리합니다.</h1>
      <p>
        사용자·조직, 역할·권한, 메뉴, 공통코드 관리 메뉴에서 승인된 기준 정보를
        조회하고 변경하세요.
      </p>
    </section>
  );
}

export function AppRouter({ isReady, permittedRoutes }: AppRouterProps) {
  const protectedRoute = (path: string, element: ReactNode) => (
    <ProtectedRoute
      isReady={isReady}
      path={path}
      permittedRoutes={permittedRoutes}
    >
      {element}
    </ProtectedRoute>
  );

  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route
        path="/system/user-organization/users"
        element={protectedRoute(
          "/system/user-organization/users",
          <UserManagementPage />,
        )}
      />
      <Route
        path="/system/user-organization/organizations"
        element={protectedRoute(
          "/system/user-organization/organizations",
          <OrganizationManagementPage />,
        )}
      />
      <Route
        path="/system/roles-permissions/roles"
        element={protectedRoute(
          "/system/roles-permissions/roles",
          <RoleManagementPage />,
        )}
      />
      <Route
        path="/system/roles-permissions/user-roles"
        element={protectedRoute(
          "/system/roles-permissions/user-roles",
          <UserRoleManagementPage />,
        )}
      />
      <Route
        path="/system/roles-permissions/menu-permissions"
        element={protectedRoute(
          "/system/roles-permissions/menu-permissions",
          <MenuPermissionManagementPage />,
        )}
      />
      <Route
        path="/system/menus/structure"
        element={protectedRoute(
          "/system/menus/structure",
          <MenuStructureManagementPage />,
        )}
      />
      <Route
        path="/system/menus/information"
        element={protectedRoute(
          "/system/menus/information",
          <MenuInformationManagementPage />,
        )}
      />
      <Route
        path="/system/common-codes/groups"
        element={protectedRoute(
          "/system/common-codes/groups",
          <CodeGroupManagementPage />,
        )}
      />
      <Route
        path="/system/common-codes/detail-codes"
        element={protectedRoute(
          "/system/common-codes/detail-codes",
          <DetailCodeManagementPage />,
        )}
      />
      <Route
        path="/system/security-audit/session-status"
        element={protectedRoute(
          "/system/security-audit/session-status",
          <SessionStatusManagementPage />,
        )}
      />
      <Route path="*" element={<Navigate replace to="/" />} />
    </Routes>
  );
}
