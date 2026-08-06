import { FormEvent, useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { screens } from "../featureCatalog";

type SessionState = "checking" | "authenticated" | "anonymous";

const menuIcons: Record<string, string> = {
  "시스템 관리": "▦",
  "파일·데이터 관리": "⇪",
  "보안·감사 관리": "◈",
  "시스템 운영 관리": "↻",
};

export function AdminShell() {
  const [session, setSession] = useState<SessionState>("checking");
  const navigate = useNavigate();

  useEffect(() => {
    api
      .me()
      .then((res) => setSession(res.success ? "authenticated" : "anonymous"))
      .catch(() => setSession("anonymous"));
  }, []);

  async function logout() {
    await api.logout().catch(() => undefined);
    setSession("anonymous");
    navigate("/login");
  }

  if (session === "checking") {
    return <div className="full-state">세션을 확인하는 중입니다.</div>;
  }

  if (session === "anonymous") {
    return <InlineLogin onDone={() => setSession("authenticated")} />;
  }

  const grouped = screens.reduce<Record<string, typeof screens>>(
    (acc, screen) => {
      const group = screen.menuPath.split(" > ")[0];
      acc[group] = [...(acc[group] || []), screen];
      return acc;
    },
    {},
  );

  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <span className="brand-kicker">KNUE FPE</span>
          <strong>공통기능 관리</strong>
          <small>source-backed 25 screens</small>
        </div>
        <nav className="sidebar-nav" aria-label="관리 메뉴">
          {Object.entries(grouped).map(([group, items]) => (
            <section className="nav-group" key={group}>
              <h2>
                <span>{menuIcons[group] ?? "•"}</span>
                {group}
              </h2>
              {items.map((item) => (
                <NavLink
                  key={item.route}
                  className={({ isActive }) =>
                    `nav-item${isActive ? " active" : ""}`
                  }
                  to={item.route}
                >
                  <span className="nav-code">
                    {item.screenId.replace("SCR-", "")}
                  </span>
                  <span>{item.menuPath.split(" > ").slice(-1)[0]}</span>
                </NavLink>
              ))}
            </section>
          ))}
        </nav>
        <button
          className="ghost-button sidebar-logout"
          onClick={logout}
          type="button"
        >
          로그아웃
        </button>
      </aside>
      <Outlet />
    </div>
  );
}

function InlineLogin({ onDone }: { onDone: () => void }) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [error, setError] = useState("");

  async function login(event: FormEvent) {
    event.preventDefault();
    setError("");
    const result = await api.login(loginId, password);
    if (result.success) {
      onDone();
      return;
    }
    setError(result.error?.message ?? "로그인에 실패했습니다.");
  }

  return (
    <main className="login-panel">
      <form onSubmit={login} className="login-card">
        <span className="brand-mark">KNUE</span>
        <h1>교수업적평가 공통기능</h1>
        <p>관리자 세션으로 25개 API-backed 화면을 확인합니다.</p>
        {error && <div className="alert error">{error}</div>}
        <label>
          아이디
          <input
            value={loginId}
            onChange={(event) => setLoginId(event.target.value)}
          />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        <button className="primary-button" type="submit">
          로그인
        </button>
      </form>
    </main>
  );
}
