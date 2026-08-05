import React from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Search } from "lucide-react";
import { api, ApiClientError } from "../../api/client";
import { firstProtectedRoute } from "../screenConfigs";
import { Session } from "../types";

const firstAllowedRoute = (session: Session) =>
  session.menus.find((menu) => menu.routePath)?.routePath ??
  firstProtectedRoute;

type Props = {
  session: Session | null;
  onLogin: (session: Session) => void;
};

export function LoginPage({ session, onLogin }: Props) {
  const navigate = useNavigate();
  const [loginId, setLoginId] = React.useState("admin");
  const [password, setPassword] = React.useState("admin");
  const [error, setError] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  if (session) return <Navigate to={firstAllowedRoute(session)} replace />;

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const next = await api<Session>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ loginId, password }),
      });
      onLogin(next);
      navigate(firstAllowedRoute(next), { replace: true });
    } catch (e) {
      setError(e instanceof ApiClientError ? e.message : "로그인 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white text-plumBlack">
      <header className="fixed inset-x-0 top-0 z-[671] flex h-20 items-center gap-2 bg-white px-4">
        <div className="flex h-12 w-12 items-center justify-center rounded-full text-2xl font-black text-pinterest">
          F
        </div>
        <div className="hidden h-12 min-w-[60px] items-center rounded-full bg-black px-4 font-semibold text-white md:flex">
          로그인
        </div>
        <div className="flex h-12 flex-1 items-center gap-2 rounded-3xl bg-warmSand px-4 text-oliveMuted">
          <Search className="h-5 w-5" />
          <span className="truncate text-base">교수업적평가 공통기능 접속</span>
        </div>
      </header>
      <main className="mx-auto grid min-h-screen max-w-6xl grid-cols-1 items-center gap-8 px-4 pt-20 md:grid-cols-[1fr_420px]">
        <section className="py-10 text-center md:text-left">
          <p className="text-sm font-bold text-pinterest">
            KNUE faculty evaluation
          </p>
          <h1 className="mt-4 text-4xl font-black leading-tight tracking-[-0.04em] md:text-6xl">
            기준정보를 핀처럼 빠르게 찾고, 필요한 필드만 저장합니다.
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-oliveMuted">
            Pinterest의 검색 우선 헤더와 둥근 카드 감각을 행정 관리 화면에 맞춰
            낮은 밀도의 조회·상세·저장 흐름으로 재구성했습니다.
          </p>
        </section>
        <form onSubmit={submit} className="pin-card p-8">
          <div className="text-center">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-pinterest text-2xl font-black text-white">
              F
            </div>
            <h2 className="mt-4 text-3xl font-bold">교수업적평가 시스템</h2>
            <p className="mt-2 text-sm text-oliveMuted">
              시드 관리자 admin/admin으로 9개 관리 메뉴에 진입합니다.
            </p>
          </div>
          <div className="mt-6 space-y-4">
            <label className="block text-sm font-semibold">
              로그인 ID
              <input
                className="pin-input mt-2"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                required
              />
            </label>
            <label className="block text-sm font-semibold">
              비밀번호
              <input
                className="pin-input mt-2"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </label>
          </div>
          {error && (
            <p className="mt-4 rounded-2xl border border-[#9e0a0a] p-3 text-sm font-semibold text-[#9e0a0a]">
              {error}
            </p>
          )}
          <button
            disabled={loading}
            className="pin-button-primary mt-6 h-12 w-full"
            type="submit"
          >
            {loading ? "로그인 확인 중" : "로그인"}
          </button>
        </form>
      </main>
    </div>
  );
}
