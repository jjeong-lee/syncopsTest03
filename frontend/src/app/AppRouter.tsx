import { Navigate, Route, Routes } from "react-router-dom";
import { AdminShell } from "../components/AdminShell";
import { LoginPage } from "../pages/LoginPage";
import { CodeGroupPage } from "../pages/system/CodeGroupPage";
import { DetailCodePage } from "../pages/system/DetailCodePage";
import { MenuInfoPage } from "../pages/system/MenuInfoPage";
import { MenuPermissionPage } from "../pages/system/MenuPermissionPage";
import { MenuStructurePage } from "../pages/system/MenuStructurePage";
import { OrganizationPage } from "../pages/system/OrganizationPage";
import { RolePage } from "../pages/system/RolePage";
import { UserPage } from "../pages/system/UserPage";
import { UserRolePage } from "../pages/system/UserRolePage";
import { RouteGuard } from "./RouteGuard";

function ProtectedShell() {
  return <AdminShell />;
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RouteGuard />}>
        <Route element={<ProtectedShell />}>
          <Route path="/system/users" element={<UserPage />} />
          <Route path="/system/organizations" element={<OrganizationPage />} />
          <Route path="/system/roles" element={<RolePage />} />
          <Route path="/system/user-roles" element={<UserRolePage />} />
          <Route
            path="/system/menu-permissions"
            element={<MenuPermissionPage />}
          />
          <Route
            path="/system/menu-structure"
            element={<MenuStructurePage />}
          />
          <Route path="/system/menu-info" element={<MenuInfoPage />} />
          <Route path="/system/code-groups" element={<CodeGroupPage />} />
          <Route path="/system/codes" element={<DetailCodePage />} />
        </Route>
      </Route>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/system/users" replace />} />
    </Routes>
  );
}
