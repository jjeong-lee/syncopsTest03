import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  Navigate,
  NavLink,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";
import { authApi } from "./api/domain";
import type { CurrentUser } from "./types";
import { navGroups, routeMeta, StatePanel } from "./ui";
import {
  CodeGroupManagementPage,
  DetailCodeManagementPage,
  MenuInfoManagementPage,
  MenuPermissionManagementPage,
  MenuStructureManagementPage,
  OrganizationManagementPage,
  RoleManagementPage,
  UserManagementPage,
  UserRoleManagementPage,
} from "./pages/ManagementPages";

export default function AppRouter() {
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [bootstrap, setBootstrap] = useState<"loading" | "idle" | "success">(
    "loading",
  );

  useEffect(() => {
    authApi
      .me()
      .then((user) => {
        setCurrentUser(user);
        setBootstrap("success");
      })
      .catch(() => setBootstrap("idle"));
  }, []);

  if (bootstrap === "loading")
    return <StatePanel state="loading" title="세션 확인 중" />;

  if (!currentUser) {
    return (
      <Routes>
        <Route
          path="/login"
          element={<LoginPage onLogin={(user) => setCurrentUser(user)} />}
        />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage onLogin={(user) => setCurrentUser(user)} />}
      />
      <Route path="/" element={<Navigate to="/system/users" replace />} />
      <Route
        path="/system/*"
        element={
          <SystemLayout
            user={currentUser}
            onLogout={() => setCurrentUser(null)}
          />
        }
      >
        <Route
          path="users"
          element={
            <GuardedPage route="/system/users" user={currentUser}>
              <UserManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="organizations"
          element={
            <GuardedPage route="/system/organizations" user={currentUser}>
              <OrganizationManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="roles"
          element={
            <GuardedPage route="/system/roles" user={currentUser}>
              <RoleManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="user-roles"
          element={
            <GuardedPage route="/system/user-roles" user={currentUser}>
              <UserRoleManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="menu-permissions"
          element={
            <GuardedPage route="/system/menu-permissions" user={currentUser}>
              <MenuPermissionManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="menu-structure"
          element={
            <GuardedPage route="/system/menu-structure" user={currentUser}>
              <MenuStructureManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="menu-info"
          element={
            <GuardedPage route="/system/menu-info" user={currentUser}>
              <MenuInfoManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="code-groups"
          element={
            <GuardedPage route="/system/code-groups" user={currentUser}>
              <CodeGroupManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="detail-codes"
          element={
            <GuardedPage route="/system/detail-codes" user={currentUser}>
              <DetailCodeManagementPage />
            </GuardedPage>
          }
        />
        <Route
          path="*"
          element={
            <StatePanel
              state="permission"
              title="범위 밖 화면"
              message="ui-design.md Screen Inventory에 없는 route입니다."
            />
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/system/users" replace />} />
    </Routes>
  );
}

function LoginPage({ onLogin }: { onLogin: (user: CurrentUser) => void }) {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const submit = (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    authApi
      .login(username, password)
      .then((user) => {
        onLogin(user);
        navigate("/system/users", { replace: true });
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };
  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-brand">
          <span>KNUE</span>
          <strong>교수업적평가시스템</strong>
        </div>
        <div className="login-form-card">
          <p className="eyebrow">공통기능 1차 시스템 관리</p>
          <h1>관리 콘솔 로그인</h1>
          <p>내부 계정 세션을 시작하면 R09 권한 기준 메뉴가 로드됩니다.</p>
          <form onSubmit={submit}>
            <label>
              아이디
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </label>
            <label>
              비밀번호
              <input
                value={password}
                type="password"
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>
            {error && (
              <div role="alert" className="inline-error">
                {error}
              </div>
            )}
            <button type="submit" disabled={loading}>
              {loading ? "로그인 중" : "로그인"}
            </button>
          </form>
        </div>
      </section>
      <aside className="login-hero" aria-hidden="true">
        <div className="dashboard-ghost">
          <span />
          {Object.values(routeMeta).map((meta) => (
            <p key={meta.screen}>
              {meta.screen} · {meta.title}
            </p>
          ))}
        </div>
      </aside>
    </main>
  );
}

function SystemLayout({
  user,
  onLogout,
}: {
  user: CurrentUser;
  onLogout: () => void;
}) {
  const location = useLocation();
  const navigate = useNavigate();
  const allowed = useMemo(
    () => new Set(user.menus.filter((m) => m.url).map((m) => m.url as string)),
    [user.menus],
  );
  const meta = routeMeta[location.pathname];
  const logout = () =>
    authApi.logout().finally(() => {
      onLogout();
      navigate("/login", { replace: true });
    });
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="brand-mark">K</div>
          <div>
            <strong>시스템 관리</strong>
            <p>R09 운영 콘솔</p>
          </div>
        </div>
        <nav aria-label="시스템 관리 메뉴">
          {navGroups.map((group) => {
            const visibleRoutes = group.routes.filter((route) =>
              allowed.has(route),
            );
            if (visibleRoutes.length === 0) return null;
            return (
              <section className="nav-group" key={group.label}>
                <p>{group.label}</p>
                {visibleRoutes.map((route) => (
                  <NavLink
                    key={route}
                    to={route}
                    className={({ isActive }) => (isActive ? "active" : "")}
                  >
                    {routeMeta[route].title}
                  </NavLink>
                ))}
              </section>
            );
          })}
        </nav>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <div>
            <span className="eyebrow">{meta?.screen ?? location.pathname}</span>
            <h1>{meta?.title ?? "권한 없음"}</h1>
          </div>
          <div className="user-chip">
            <span>{user.username}</span>
            <span>{user.roles.join(", ")}</span>
            <button type="button" className="secondary" onClick={logout}>
              로그아웃
            </button>
          </div>
        </header>
        <Outlet />
      </section>
    </div>
  );
}

function GuardedPage({
  route,
  user,
  children,
}: {
  route: string;
  user: CurrentUser;
  children: React.ReactNode;
}) {
  const allowed = user.menus.some((menu) => menu.url === route);
  if (!allowed)
    return (
      <StatePanel
        state="permission"
        title="권한 없음"
        message="접근 권한이 없는 메뉴이거나 비활성화된 화면입니다."
      />
    );
  return <>{children}</>;
}
