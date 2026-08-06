import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { screens } from "../featureCatalog";

export function LoginPage() {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function login(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");
    const result = await api
      .login(loginId, password)
      .finally(() => setLoading(false));
    if (result.success) {
      navigate(screens[0].route);
      return;
    }
    setError(result.error?.message ?? "아이디 또는 비밀번호를 확인하세요.");
  }

  return (
    <main className="login-panel split-login">
      <form onSubmit={login} className="login-card">
        <span className="brand-mark">KNUE</span>
        <h1>교수업적평가 공통기능</h1>
        <p>API와 DB에 연결된 공통 관리 화면으로 이동합니다.</p>
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
        <button className="primary-button" disabled={loading} type="submit">
          {loading ? "확인 중" : "로그인"}
        </button>
      </form>
      <section className="login-hero" aria-hidden="true">
        <div className="hero-card">
          <strong>R09 시스템관리자</strong>
          <span>사용자·권한·파일·감사·배치 흐름을 한 곳에서 관리</span>
        </div>
      </section>
    </main>
  );
}
