import { useEffect, useMemo, useState } from "react";
import {
  Link,
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
} from "react-router-dom";
import {
  ChevronRight,
  Database,
  LockKeyhole,
  LogOut,
  Menu as MenuIcon,
  RefreshCcw,
  ShieldCheck,
} from "lucide-react";
import { api, CurrentUser } from "../api/client";
import {
  CodeGroupsPage,
  DetailCodesPage,
  MenuInfoPage,
  MenuPermissionsPage,
  MenuStructurePage,
  OrganizationsPage,
  RolesPage,
  UserRolesPage,
  UsersPage,
} from "./ScreenPages";
import { screens } from "./screens";

export function AppRouter() {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);
  const currentUserId = user?.userId ?? "";

  useEffect(() => {
    api
      .me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <FullScreenState
        type="loading"
        title="세션 확인 중"
        message="현재 사용자와 메뉴 권한을 확인하고 있습니다."
      />
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage onLogin={setUser} user={user} />}
      />
      <Route
        path="/"
        element={<Navigate to={user ? "/system/users" : "/login"} replace />}
      />
      <Route
        path="/system/*"
        element={
          <RequireAdmin user={user} onLogout={() => setUser(null)}>
            <Routes>
              <Route
                path="users"
                element={<UsersPage currentUserId={currentUserId} />}
              />
              <Route
                path="organizations"
                element={<OrganizationsPage currentUserId={currentUserId} />}
              />
              <Route
                path="roles"
                element={<RolesPage currentUserId={currentUserId} />}
              />
              <Route
                path="user-roles"
                element={<UserRolesPage currentUserId={currentUserId} />}
              />
              <Route
                path="menu-permissions"
                element={<MenuPermissionsPage currentUserId={currentUserId} />}
              />
              <Route
                path="menu-structure"
                element={<MenuStructurePage currentUserId={currentUserId} />}
              />
              <Route
                path="menu-info"
                element={<MenuInfoPage currentUserId={currentUserId} />}
              />
              <Route
                path="code-groups"
                element={<CodeGroupsPage currentUserId={currentUserId} />}
              />
              <Route
                path="code-groups/:groupId/codes"
                element={<DetailCodesPage currentUserId={currentUserId} />}
              />
              <Route
                path="*"
                element={<Navigate to="/system/users" replace />}
              />
            </Routes>
          </RequireAdmin>
        }
      />
      <Route
        path="*"
        element={<Navigate to={user ? "/system/users" : "/login"} replace />}
      />
    </Routes>
  );
}

function RequireAdmin({
  user,
  onLogout,
  children,
}: {
  user: CurrentUser | null;
  onLogout: () => void;
  children: React.ReactNode;
}) {
  if (!user) return <Navigate to="/login" replace />;
  if (!user.roles.includes("R09")) {
    return (
      <FullScreenState
        type="permission"
        title="권한 없음"
        message="R09 시스템관리자 권한이 있어야 시스템 관리 화면에 접근할 수 있습니다."
      />
    );
  }
  return (
    <ProtectedLayout user={user} onLogout={onLogout}>
      {children}
    </ProtectedLayout>
  );
}

function LoginPage({
  onLogin,
  user,
}: {
  onLogin: (user: CurrentUser) => void;
  user: CurrentUser | null;
}) {
  const navigate = useNavigate();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (user) navigate("/system/users", { replace: true });
  }, [navigate, user]);

  return (
    <main className="login-shell">
      <section className="login-copy" aria-hidden="true">
        <div className="login-copy-card">
          <p className="eyebrow">KNUE FPE COMMON</p>
          <h1>시스템 관리의 기준정보를 한 곳에서 정렬합니다.</h1>
          <p>
            사용자, 조직, 역할, 메뉴, 공통코드를 실제 API 응답 기준으로 확인하고
            변경 이력을 남깁니다.
          </p>
        </div>
      </section>
      <form
        className="login-card"
        onSubmit={async (event) => {
          event.preventDefault();
          setBusy(true);
          setError("");
          try {
            const current = await api.login(username, password);
            onLogin(current);
            navigate("/system/users", { replace: true });
          } catch (e) {
            setError(e instanceof Error ? e.message : "로그인 실패");
          } finally {
            setBusy(false);
          }
        }}
      >
        <div className="brand-mark">
          <ShieldCheck size={20} /> 한국교원대학교
        </div>
        <div>
          <h2>교수업적평가시스템</h2>
          <p>공통기능 1차 시스템 관리 로그인</p>
        </div>
        <label>
          아이디
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>
        {error && (
          <div className="state error compact" role="alert">
            {error}
          </div>
        )}
        <button className="primary-button" disabled={busy} type="submit">
          {busy ? "로그인 중..." : "로그인"}
        </button>
      </form>
    </main>
  );
}

function ProtectedLayout({
  user,
  onLogout,
  children,
}: {
  user: CurrentUser;
  onLogout: () => void;
  children: React.ReactNode;
}) {
  const location = useLocation();
  const navigate = useNavigate();
  const params = useParams();
  const groups = useMemo(
    () =>
      ["사용자·조직 관리", "역할·권한 관리", "메뉴 관리", "공통코드 관리"].map(
        (group) => ({
          group,
          items: screens.filter((screen) => screen.group === group),
        }),
      ),
    [],
  );

  const logout = async () => {
    await api.logout().catch(() => undefined);
    onLogout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo-square">
            <Database size={18} />
          </div>
          <div>
            <strong>FPE Common</strong>
            <span>System Admin</span>
          </div>
        </div>
        <nav className="sidebar-nav" aria-label="시스템 관리 메뉴">
          <p className="nav-root">
            <MenuIcon size={15} /> 시스템 관리
          </p>
          {groups.map((group) => (
            <section key={group.group} className="nav-group">
              <p>{group.group}</p>
              {group.items.map((screen) => {
                const to =
                  screen.kind === "detailCodes"
                    ? params.groupId
                      ? `/system/code-groups/${params.groupId}/codes`
                      : "/system/code-groups"
                    : screen.route;
                const active = matchRoute(screen.route, location.pathname);
                return (
                  <Link
                    className={active ? "active" : ""}
                    key={screen.id}
                    to={to}
                  >
                    <ChevronRight size={14} />
                    {screen.title}
                  </Link>
                );
              })}
            </section>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-card">
            <LockKeyhole size={16} />
            <span>{user.username}</span>
            <small>{user.roles.join(", ")}</small>
          </div>
          <button className="ghost-button full" onClick={logout} type="button">
            <LogOut size={16} /> 로그아웃
          </button>
        </div>
      </aside>
      <main className="content">
        <header className="topbar">
          <div>
            <span>API-backed Admin</span>
            <strong>{new Date().toLocaleDateString("ko-KR")}</strong>
          </div>
          <button
            className="ghost-button"
            onClick={() => window.location.reload()}
            type="button"
          >
            <RefreshCcw size={15} /> 새로고침
          </button>
        </header>
        {children}
      </main>
    </div>
  );
}

function matchRoute(route: string, pathname: string) {
  if (route.includes(":groupId"))
    return /^\/system\/code-groups\/[^/]+\/codes$/.test(pathname);
  return route === pathname;
}

function FullScreenState({
  type,
  title,
  message,
}: {
  type: string;
  title: string;
  message: string;
}) {
  return (
    <main className="full-state">
      <div className={`state ${type}`} role="status">
        <strong>{title}</strong>
        <span>{message}</span>
      </div>
    </main>
  );
}
